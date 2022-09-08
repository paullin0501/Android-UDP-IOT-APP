package com.rexlite.rexlitebasicnew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UDP implements Runnable {
    public static final String TAG = "MyUDP";
    public static final String RECEIVE_ACTION = "GetUDPReceive";
    public static final String RECEIVE_STRING = "ReceiveString";
    public static final String RECEIVE_BYTES = "ReceiveBytes";
    public static final String RECEIVE_DATALENGTH = "ReciveDataLength";

    private int port = 4000;
    private String ServerIp;
    private boolean isOpen;
    private static DatagramSocket ds = null;
    private Context context;

    /**切換伺服器監聽狀態*/
    public void changeServerStatus(boolean isOpen) {
        this.isOpen = isOpen;
        if (!isOpen) {
            ds.close();
            Log.e(TAG, "UDP-Server已關閉");
        }
    }
    //切換Port
    public void setPort(int port){
        this.port = port;
    }
    /**初始化建構子*/
    public UDP(String ServerIp,Context context) {
        this.context = context;
        this.ServerIp = ServerIp;
        this.isOpen = true;

    }
    /**發送訊息*/
    public void send(byte[] string, String remoteIp, int remotePort) throws IOException {
        Log.d(TAG, "客户端IP：" + remoteIp + ":" + remotePort);
        InetAddress inetAddress = InetAddress.getByName(remoteIp);
        DatagramSocket datagramSocket = new DatagramSocket();
        DatagramPacket dpSend = new DatagramPacket(string, string.length, inetAddress, remotePort);
        datagramSocket.send(dpSend);

    }
    /**發送訊息(發送JSON)*/
    public void send(String string, String remoteIp, int remotePort) throws IOException {
        Log.d(TAG, "客户端IP：" + remoteIp + ":" + remotePort);
        InetAddress inetAddress = InetAddress.getByName(remoteIp);
        DatagramSocket datagramSocket = new DatagramSocket();
        DatagramPacket dpSend = new DatagramPacket(string.getBytes(), string.getBytes().length, inetAddress, remotePort);
        datagramSocket.send(dpSend);

    }
    /**監聽執行緒*/
    @Override
    public void run() {
        /**在本機上開啟Server監聽*/
        InetSocketAddress inetSocketAddress = new InetSocketAddress("0.0.0.0", port); //0.0.0.0是不設定預設IP的意思
        try {
            ds = new DatagramSocket(inetSocketAddress);
            Log.e(TAG, "UDP-Server已啟動");
        } catch (SocketException e) {
            Log.e(TAG, "啟動失敗，原因: " + e.getMessage());
            e.printStackTrace();
        }
        //預備一組byteArray來放入回傳得到的值(PS.回傳為格式為byte[])
        byte[] msgRcv = new byte[2048];
        DatagramPacket dpRcv = new DatagramPacket(msgRcv, msgRcv.length);
        //建立while迴圈持續監聽來訪的數值
        while (isOpen) {
            Log.e(TAG, "UDP-Server監聽資訊中..");
            try {
                //執行緒將會在此打住等待有值出現
                ds.receive(dpRcv);
                String string = new String(dpRcv.getData(), dpRcv.getOffset(), dpRcv.getLength());
                Log.d(TAG, "UDP-Server收到資料： " + string);
                Log.d(TAG, "size: "+dpRcv.getLength());
                /**以Intent的方式建立廣播，將得到的值傳至主要Activity*/
                Intent intent = new Intent();
                intent.setAction(RECEIVE_ACTION);
                intent.putExtra(RECEIVE_STRING,string);
                intent.putExtra(RECEIVE_BYTES, dpRcv.getData());
                Bundle bundle = new Bundle();
                bundle.putInt(RECEIVE_DATALENGTH, dpRcv.getLength());// dpRcv.getLength()可拿到實際資料的長度
                intent.putExtras(bundle);
                context.sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //小寫
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
        }
        return sb.toString();
    }

    public static byte[] hexToByte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i=0 ; i<bytes.length ; i++)
            bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }
    //大寫
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    static String hexToBin(String s) {
        int i = Integer.parseInt(s, 16);
        String bin = Integer.toBinaryString(i);
        while (bin.length()<8){
            bin="0"+bin;
        }
        return bin;
        // s = new BigInteger(s, 16).toString(2);
       // return String.format("%8s", Integer.toBinaryString(s)).replace(' ', '0');
    }
}
