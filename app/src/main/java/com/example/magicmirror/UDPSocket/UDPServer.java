package com.example.magicmirror.UDPSocket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

/**
 * Created by Administrator on 2015/11/18.
 */
public class UDPServer {
    private MulticastSocket udpSocket;
    private int port = 10001;
    private Boolean isStop = false;
    private Boolean isClose = false;

    private WifiManager.MulticastLock lock;

    private EditText info;
    public UDPServer(Context context){
        try {
            this.udpSocket = new MulticastSocket(port);
            this.udpSocket.setBroadcast(true);
            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            lock = manager.createMulticastLock("multicastLock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop(){
        if(!this.isStop){
            this.isStop = true;
        }
    }
    public boolean getIsStop(){
        return this.isStop;
    }
    public synchronized void resume(){
        if(this.isStop){
            this.isStop = false;
        }
    }
    public synchronized void close(){
        if(!this.isClose){
            this.udpSocket.close();
            this.isClose = true;
        }
    }
    public synchronized void acquirelock(){
        if (lock != null && !lock.isHeld()) {
            lock.acquire();
        }
    }
    protected void finallize() throws Throwable {
        close();
        super.finalize();
    }
    public DatagramPacket receive() {
        byte[] buf = new byte[3000];
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        try {
            acquirelock();
            udpSocket.setBroadcast(true);
            udpSocket.receive(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return datagramPacket;
        }
    }
}
