package com.test.threads;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.networkbroard.bean.Datapacket;
import com.example.networkbroard.utils.SharePreferenceSetting;
import com.google.gson.reflect.TypeToken;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendDataThread extends Thread {
    Socket socket;

    String data = "";
    String address;
    Context context;
    Handler handler;
    File file;
    int port;

    public SendDataThread(String address, String data, Context context) {

        Log.i("SendDataThread", "address:" + address + ";" + data);
        this.address = address;
        this.data = data;
        this.context = context;
        port=12345;

    }


    public SendDataThread(String address, Context context,Handler handler,File file) {

        Log.i("SendDataThread", "address:" + address + ";" + data);
        this.address = address;
        this.data="File";
        this.context = context;
        this.handler=handler;
        this.file=file;
        port=12346;

    }


    @Override
    public void run() {
        try {

            socket = new Socket(address, port);//发送到本机下某个Ip的端口上
        } catch (UnknownHostException e) {
            Log.d("aaa", "SendDataThread.init() has UnknownHostException" + e.getMessage());
        } catch (IOException e) {
            Log.d("aaa", "SendDataThread.init().IOException:" + e.getMessage());
        }
        if (socket != null) {

//
//            try {
//
//
//
//			/*
//            //原来的发送数据
//			PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
//				out.println(data);  //发送数据
//
//				*/
//                sendObject(socket);
//
//
//				/*oos.flush();
//
//				oos.close();*/
//                Log.i("aaa", "msg is send");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                Log.d("aaa", "out.println:" + e.toString());
//            }

            if(data.equals("File")&&  port==12346) {

                sendFile(socket, this.file);
            }
            else if(data.equals("filemsg"))
            {
                Log.i("send","filemsg is send start");
                sendfilemsgObject(socket);
            }
            else
            {
                sendObject(socket);
            }



        } else {
            Log.d("aaa", "socket is null");
        }

    }


    /**
     * 发送对象
     * @param socket
     */
    public void sendObject(Socket socket) {
        WifiManager my_wifiManager=((WifiManager) context.getSystemService(Context.WIFI_SERVICE));;

        DhcpInfo dhcpInfo;
        WifiInfo wifiInfo;
        dhcpInfo = my_wifiManager.getDhcpInfo();
        wifiInfo = my_wifiManager.getConnectionInfo();

        try {
            //发送对象
            ObjectOutputStream oos = new ObjectOutputStream(socket
                    .getOutputStream());

            Datapacket packet = new Datapacket();
            String usernamex = (String) SharePreferenceSetting.getSp("Userinfo", "username", context, new TypeToken<String>() {
            }.getType());
            packet.setHostname(usernamex);
            packet.setIp(intToIp( dhcpInfo.ipAddress));

           /// packet.setIp(address);

            packet.setCommand(data);
            oos.writeObject(packet);        //输入对象, 一定要flush（）
            oos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("aaa", "out.println:" + e.toString());
        }

    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }


    /**
     * 发送文件信息
     * @param socket
     */
    public void sendfilemsgObject(Socket socket) {

        try {
            //发送对象
            ObjectOutputStream oos = new ObjectOutputStream(socket
                    .getOutputStream());

            Datapacket packet = new Datapacket();
            String usernamex = (String) SharePreferenceSetting.getSp("Userinfo", "username", context, new TypeToken<String>() {
            }.getType());
          String filepath=(String)SharePreferenceSetting.getSp("usertemp", "filepath", context,  new TypeToken<String>() {
            }.getType());

            File file=new File(filepath);
            if(file.exists())
            {
                Log.i("send","filemsg is exit");
                packet.setFilename(file.getName());
                packet.setFilesize(file.length());
                Log.i("send", "filemsg:"+file.getName()+";"+file.length());
            }

            packet.setHostname(usernamex);
            packet.setIp(address);
            packet.setCommand(data);
            oos.writeObject(packet);        //输入对象, 一定要flush（）
            oos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("send", "send filemsg exception" + e.toString());
        }

    }


    /**
     * 发送文件
     * @param socket
     * @param file
     */
    public void sendFile(Socket socket,File file) {
        int length = 0;
        double sumL = 0 ;
        byte[] sendBytes = null;

        DataOutputStream dos = null;
        FileInputStream fis = null;
        boolean bool = false;




        try {
            if(socket!=null) {

               // File file = new File("D:/天啊.zip"); //要传输的文件路径
                if(file!=null&&file.exists()) {
                    long l = file.length();
                 //   socket.connect(new InetSocketAddress(address, 12346));
                    dos = new DataOutputStream(socket.getOutputStream());
                    fis = new FileInputStream(file);
                    sendBytes = new byte[1024];
                    while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                        sumL += length;

                        //  System.out.println("已传输：" + ((sumL / l) * 100) + "%");
                        int percent = (int) ((sumL / l) * 100);
                        Log.i("sendpercent", percent+"");
                        Message msg=new Message();
                        msg.what = 2;
                        msg.obj = percent;
                        this.handler.sendMessage(msg);
                        dos.write(sendBytes, 0, length);
                        dos.flush();
                    }
                    //虽然数据类型不同，但JAVA会自动转换成相同数据类型后在做比较
                    if (sumL == l) {
                        bool = true;
                    }

                /*    if (dos != null)
                        dos.close();
                    if (fis != null)
                        fis.close();
                    if (socket != null)
                        socket.close();
*/
                }
            }
        }catch (Exception e) {

            //客户端文件传输异常
            Message msg=new Message();
            msg.what = 3;
            msg.obj = 400;
            this.handler.sendMessage(msg);
            //System.out.println("客户端文件传输异常");
            bool = false;
            e.printStackTrace();



        } finally{

          try {

              if (dos != null)
                  dos.close();
              if (fis != null)
                  fis.close();
              if (socket != null)
                  socket.close();
          }catch (IOException e)
          {
              e.printStackTrace();
          }

        }


    }


}
