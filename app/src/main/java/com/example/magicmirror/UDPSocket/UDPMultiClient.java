package com.example.magicmirror.UDPSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/11/18.
 */
public class UDPMultiClient {
    private MulticastSocket udpSockect;
    private boolean isClose;
    private int port = 9999;
    private String multicastIp = "239.0.0.1";
    private String UdpIp = "239.1.1.1";
    private InetAddress group;
    public UDPMultiClient(){
        try {
            this.udpSockect = new MulticastSocket(port);
            group = InetAddress.getByName(multicastIp);
            this.udpSockect.joinGroup(group);
            //this.udpSockect.setBroadcast(true);
            this.udpSockect.setTimeToLive(2);
            this.isClose = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void close(){
        if(!this.isClose){
            this.udpSockect.close();
            this.isClose = true;
        }
    }

    protected void finallize() throws Throwable {
        close();
        super.finalize();
    }

    //发送数据
    public void sendData(byte[] data , String targetHostName, int targetPort, long interval){
        sendData(data, 0, data.length, targetHostName, targetPort, interval);
    }
    //发送数据带有偏移量offset
    public void sendData(byte[] data, int offset, int count, String targetHostName, int targetPort, long interval){
        //DatagramPacket datagramPacket = new DatagramPacket(data1[0],1,1,5000);
        //this.udpSockect.send();
    }
    public void send(byte[] data ,String MulticastIp){
        try {
            DatagramPacket datagramPacket = new DatagramPacket(data,data.length, InetAddress.getByName(MulticastIp),port);
            this.udpSockect.send(datagramPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
