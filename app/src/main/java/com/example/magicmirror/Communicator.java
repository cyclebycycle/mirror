package com.example.magicmirror;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Communicator extends Thread {
        
    private MainActivity mContext;
    private int mDestPort = 0;
    private DatagramSocket mSocket;
    private String TAG = "Communicator";
    private boolean running = false;
    //private boolean isBroadCast = false;
    private final int mRecvBufLen = 512;
    private int mRecvLen = 0;
    private ServerThread server = null;
    private long lastSockBeatTime = 0;
    private long lastSockRecvTime = 0;
    private long currentSockRecvTime = 0;
    private String fileName;

    //private boolean updateReady = false;
    private int otaUpdateStatus;
    private final int OTA_BEGIN = 0;
    private final int OTA_READY = 1;
    private final int OTA_RECEIVED = 2;
    private final int OTA_UPDATING = 3;
    private final int OTA_SUCCESS = 4;
    private final int OTA_FAIL = 5;

    private Thread thread;
    
    public Communicator(MainActivity activity)  {
        mContext = activity;        
    }
    
    public boolean open(int localport,int destport) {
        mDestPort = destport;   
        running = true;
        try {
            mSocket = new DatagramSocket(localport);
            mSocket.setBroadcast(true);
            mSocket.setReuseAddress(true);
            mSocket.setSoTimeout(2000);

            server = new ServerThread();
            new Thread(server).start();

            return true;
        } 
        catch (SocketException e) {
            e.printStackTrace();
        }        
        return false;
    }

   /*public void setBroadCast(boolean b)
    {
        isBroadCast = b;
    }*/

   public boolean startBind()
   {
       String name = LoginActivity.username;
       String id = LoginActivity.mirror_id;
       String bindSendStr = "Mirror:bind,name:"+name+",mirrorid:"+id;

       if((server != null) && server.getClientSocket() != null)
       {
           server.sendMeg(bindSendStr.getBytes());
           return true;
       }
       else
       {
           Log.d(TAG, "send bind message fail,server or socket is null!!!");
           return false;
       }
   }

    public boolean startUpdate(String filePath)
    {
        String updateSendStr = "Mirror:updt,file:full_img.fex";

        if((server != null) && server.getClientSocket() != null)
        {
            otaUpdateStatus = OTA_BEGIN;//updateReady = false;
            server.sendMeg(updateSendStr.getBytes());

            fileName = filePath;
            /*new Thread(){
                public void run(){
                    handler.post(sendFileRunable);
                }
            }.start();*/
            thread = new Thread(sendFileRunable);
            thread.start();
            return true;
        }
        else
        {
            Log.d(TAG, "send update message fail,server or socket is null!!!");
            return false;
        }
    }

    //private Handler handler = new Handler() {};
    Runnable sendFileRunable = new Runnable() {
        int bufferSize = 8192;
        byte[] buf = new byte[bufferSize];
        int count = 20;

        @Override
        public void run() {
            Log.e(TAG,"started send firmware thread!!!");

            /*wait device ready to receive firmware*/
            while(count > 0) {
                if(otaUpdateStatus == OTA_READY)
                {
                    mContext.updateCallback(OTA_READY, 0);
                    break;
                }
                else {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count--;
                }
            }
            if(count == 0)
            {
                mContext.updateCallback(OTA_FAIL, 0);
                return;
            }

            /*begin to send firmware*/
            File file = new File(fileName);
            Log.e(TAG,"begin to send firmware("+fileName+"),length=" + file.length());
            DataInputStream fis = null;
            try {
                fis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
                DataOutputStream dos = new DataOutputStream(server.getClientSocket().getOutputStream());

                String data = new String("Mirror:updt,length:"+file.length());
                dos.write(data.getBytes(),0,data.length());
                dos.flush();

                //Log.e(TAG,"send data:"+data);
                //server.sendMeg(data.getBytes());

                while (true) {
                    int read = 0;
                    if (fis != null) {
                        java.util.Arrays.fill(buf,(byte)0);
                        read = fis.read(buf);
                    }
                    if (read == -1) {
                        break;
                    }
                    dos.write(buf,0,read);
                    dos.flush();
                    //Log.e(TAG,"send data length:" + read);
                    //server.sendMeg(buf);
                }
                //dos.flush();
                // 注意关闭socket链接哦，不然客户端会等待server的数据过来，
                // 直到socket超时，导致数据不完整。
                fis.close();
                //dos.close();
                Log.d(TAG,"firmware send successfully!!!");
                //mContext.updateCallback(1);
            } catch (FileNotFoundException e) {
                Log.d(TAG,"error,FileNotFoundException!!!");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG,"error,IOException!!!");
                e.printStackTrace();
            }

            /*wait confirmed mesage from device*/
            count = 250;
            while(count > 0) {
                //Log.d(TAG,"otaUpdateStatus=" + otaUpdateStatus);
                if(otaUpdateStatus == OTA_RECEIVED)
                {
                    mContext.updateCallback(OTA_RECEIVED, 0);
                    otaUpdateStatus = OTA_UPDATING;
                    //Log.d(TAG,"firmware send successfully!!!");
                }
                else if(otaUpdateStatus == OTA_UPDATING)
                {
                    mContext.updateCallback(OTA_UPDATING, count);
                }
                else if(otaUpdateStatus == OTA_SUCCESS)
                {
                    mContext.updateCallback(OTA_SUCCESS, 0);
                    break;
                }
                else if(otaUpdateStatus == OTA_FAIL)
                {
                    mContext.updateCallback(OTA_FAIL, 0);
                    break;
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;

            }
            if(count == 0)
            {
                mContext.updateCallback(OTA_FAIL, 0);
            }
        }
    };

    private int matchStringByIndexOf( String parent,String child,int[] in)
    {
        int count = 0;
        int index = 0;
        while( ( index = parent.indexOf(child, index) ) != -1 )
        {
            in[count] = index;
            Log.d(TAG,"matchStringByIndexOf:"+ count + "," + index);
            index = index+child.length();
            count++;
        }
        return count;
    }

    public void run(){  
    	Log.d(TAG, "run");

        String udpAckStr = "MagicMirror:ack";
        String udpRecvStr = "MagicMirror:started";

    	String wifiRecvStr = "Mirror:wifi,state:configed";

    	String bindRecvStr = "Mirror:bind,state:binded";

    	String beatRecvStr = "Mirror:beat";

        String updateReadyStr = "Mirror:updt,state:ready";
        String updateRecvedStr = "Mirror:updt,state:received";
        String updateSuccessStr = "Mirror:updt,state:success";
        String updateFailStr = "Mirror:updt,state:fail";

    	byte[] recvBuf = new byte[mRecvBufLen];
    	String recvStr;
    	int len = 0;
    	int errorCount = 0;
    	int msgCount = 0;
    	int[] msgArray = new int[10];

    	while(running)
    	{
                Log.d(TAG, "begin UDP communication at port:" + mDestPort);
                java.util.Arrays.fill(recvBuf,(byte)0);
                if(recvPacket(recvBuf))
                {
                    try {
                        recvStr = new String(recvBuf,0,mRecvLen,"GBK");

                        Log.d(TAG, "receive udp data[" + recvStr + "] from mirror,length="+mRecvLen);
                        if(recvStr.equals(udpRecvStr))
                        {
                            sendPacket(udpAckStr.getBytes());;
                        }
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.d(TAG, "receive UDP timeout!!!");
                    continue;
                }

            if(!running)
            {
                break;
            }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            if(!running)
            {
                break;
            }
                if(server.getClientSocket() == null)
                {//if socket not connect,wait 1 sec again
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            if(!running)
            {
                break;
            }
                if(server.getClientSocket() == null)
                {//if socket not connect,wait 1 sec again
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            if(!running)
            {
                break;
            }
            lastSockRecvTime = lastSockBeatTime = System.currentTimeMillis();
            errorCount = 0;
            while(server.getClientSocket() != null)
            {
                java.util.Arrays.fill(recvBuf,(byte)0);
                len = server.recvMeg(recvBuf,mRecvBufLen);
                //recvStr = server.recvMeg();
                currentSockRecvTime = System.currentTimeMillis();
                if(len <= 0)
                {
                    Log.d(TAG,"socket receive message length=" +  len);
                    if((currentSockRecvTime - lastSockRecvTime) < 200)
                    {
                        errorCount++;
                        if(errorCount > 10)
                        {
                            server.closeClientSocket();
                            break;
                        }
                    }
                    else
                    {
                        errorCount = 0;
                    }

                    if((currentSockRecvTime - lastSockBeatTime) > 16*1000)
                    {//client socket heart beat reflash more than 16 seccond
                        server.closeClientSocket();
                        break;
                    }
                    lastSockRecvTime = currentSockRecvTime;
                    continue;
                }

                Log.d(TAG,"socket receive message:" + new String(recvBuf, 0, len));
                msgCount = matchStringByIndexOf(new String(recvBuf, 0, len), new String("Mirror:"), msgArray);
                for(int i=0; i<msgCount; i++) {
                    try {
                        if(i == (msgCount - 1)) {
                            recvStr = new String(recvBuf, msgArray[i], len-msgArray[i], "GBK");
                        }
                        else
                        {
                            recvStr = new String(recvBuf, msgArray[i], msgArray[i+1]-msgArray[i], "GBK");
                        }

                        if (recvStr.equals(wifiRecvStr)) {//wifi message
                            mContext.wifiCallback();
                        } else if (recvStr.equals(bindRecvStr)) {//bind message
                            mContext.bindCallback();
                        } else if (recvStr.equals(beatRecvStr)) {//heart beat message
                            lastSockBeatTime = System.currentTimeMillis();
                        } else if (recvStr.equals(updateReadyStr)) {//ota update ready
                            otaUpdateStatus = OTA_READY;
                        } else if (recvStr.equals(updateRecvedStr)) {//ota update received
                            otaUpdateStatus = OTA_RECEIVED;
                        } else if (recvStr.equals(updateSuccessStr)) {//ota update successful
                            otaUpdateStatus = OTA_SUCCESS;
                        } else if (recvStr.equals(updateFailStr)) {//ota pduate fail
                            otaUpdateStatus = OTA_FAIL;
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                /*try {
                    server.sendMeg("receive OK Ack".getBytes("utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
            }
    	}
    } 
    
    public boolean close() {
    	running = false;
        //isBroadCast = false;

        this.interrupt();
        if(mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
        }

        if(server != null)
        {
            server.stop();
        }

        return true;
    }
    
    public boolean sendPacket(byte[] buffer) {
        try {
            InetAddress addr = getBroadcastAddress(mContext);
            Log.d(TAG, "getAddress=" + addr.getAddress());
        	//InetAddress addr = InetAddress.getByName("192.168.1.117");
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
            packet.setAddress(addr);
            packet.setPort(mDestPort);
            mSocket.send(packet);
            return true;
        } 
        catch (UnknownHostException e1) {
            e1.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean recvPacket(byte[] buf) {
    	//byte[] recvBuf = new byte[128];
        DatagramPacket packet = new DatagramPacket(buf,mRecvBufLen-1);
        try {
            mSocket.receive(packet);
            mRecvLen = packet.getLength();
            //Log.d(TAG, new String(packet.getData(),0,packet.getLength(),"GBK"));
            return true;
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        return false;
    }
    
    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        /*if(isWifiApEnabled(context)) {
            return InetAddress.getByName("192.168.43.255");            
        }*/
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp==null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }          
        return InetAddress.getByAddress(quads);
    }  
    
    /*protected static Boolean isWifiApEnabled(Context context) {        
        try {
            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            return (Boolean)method.invoke(manager);
        } 
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {         
            e.printStackTrace();
        }
        return false;
    }*/
    public class ServerThread implements Runnable {

        private ServerSocket server;
        private int port = 10086;
        private boolean isExit = false;// 一个boolean类型的判断 默认是退出状态false
        //private InputStream inputStream = null;
        //private OutputStream outputStream = null;
        //private BufferedReader reader;
        private Socket clientSock = null;
        // 构造方法初始化
        public ServerThread() {
            try {
                server = new ServerSocket(port);
                Log.d(TAG,"start server,port：" + port);
            } catch (IOException e) {
                e.printStackTrace();
                server = null;
            }
        }

        /**
         * 1.获得远程服务器的IP 地址.
         * InetAddress inetAddress = socket.getInetAddress();
         * 2.获得远程服务器的端口.
         * int port = socket.getPort();
         * 3. 获得客户本地的IP 地址.
         * InetAddress localAddress = socket.getLocalAddress();
         * 4.获得客户本地的端口.
         * int localPort = socket.getLocalPort();
         * 5.获取本地的地址和端口号
         * SocketAddress localSocketAddress = socket.getLocalSocketAddress();
         * 6.获得远程的地址和端口号
         * SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
         */
        @Override
        public void run() {
            if(server == null)
            {
                return;
            }
            while (!isExit) {
                try {
                    // 等待连接
                    Log.d(TAG,"wait socket connecting... ...");
                    Socket sock = server.accept();

                    if(sock == null)
                    {
                        Log.d(TAG,"accecpt socket is null,continue!");
                        continue;
                    }

                    synchronized(server) {
                            if (clientSock != null) {
                                //reader.close();
                                clientSock.close();
                            }
                            clientSock = sock;

                            clientSock.setSoTimeout(3000);
                    }
                    //inputStream = clientSock.getInputStream();
                    //utputStream = clientSock.getOutputStream();
                    //reader = new BufferedReader(new InputStreamReader(clientSock.getInputStream(), "UTF-8"));
                    Log.d(TAG,"ClientIP address and port：" + clientSock.getRemoteSocketAddress().toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "server socket thread exit!!!");
                }
            }
        }

        public Socket getClientSocket()
        {
            return clientSock;
        }

        public void closeClientSocket()
        {
            if (clientSock != null) {
                try {
                    //reader.close();
                    clientSock.close();
                    Log.d(TAG, "close client socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientSock = null;
            }
        }

        public void sendMeg(byte[] buf)
        {
            synchronized(server) {
                if (clientSock != null) {
                    try {
                        clientSock.getOutputStream().write(buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public int recvMeg(byte[] buf, int size)
        {
            int len = -1;
            Socket sock;
            //String buf;

            synchronized (server) {
                if (clientSock != null) {
                    sock = clientSock;
                }
                else {
                    return len;
                }
            }

            try {
                //return reader.readLine();
                len = sock.getInputStream().read(buf, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return len;
        }

        // 关闭server
        public void stop() {
            isExit = true;
            synchronized (server) {
                if (clientSock != null) {
                    try {
                        clientSock.close();
                        Log.d(TAG, "close client socket");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clientSock = null;
                }
            }

            if (server != null) {
                try {
                    //reader.close();
                    server.close();
                    Log.d(TAG,"close server socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //server = null;
            }
        }
    }
}