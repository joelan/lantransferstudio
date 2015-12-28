package com.example.networkbroard;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.bean.Datapacket;
import com.example.networkbroard.utils.SharePreferenceSetting;
import com.google.gson.reflect.TypeToken;
import com.test.service.LocalService;
import com.test.threads.SendDataThread;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SendProgress extends BaseActivity {

    /**
     * 服务端
     */
    //udp
    boolean client = true;
    boolean server = false;
    private static String ip; // 服务端ip
    private static int BROADCAST_PORT = 9898;
    private static String BROADCAST_IP = "224.0.0.1";
    InetAddress inetAddress = null;
    Thread t = null;
    /* 发送广播端的socket */
    MulticastSocket multicastSocket = null;
    @Bind(R.id.sendboardcastto)
    TextView sendboardcastto;

    @Bind(R.id.cancelendboardcast)
    Button cancelendboardcast;

    @Bind(R.id.progressBar2)
    ProgressBar progressBar2;

    @Bind(R.id.progresstext)
    TextView progresstext;

    /* 发送广播的按钮 */
    private Button sendUDPBrocast;
    Button ReceiveUDPbrocast;
    private volatile boolean isRuning = true;
    TextView ipInfo;
    Thread td;
    boolean issending = false;
    boolean isreceiceing = false;
    Datapacket db;

    Button sendmsg;
    String removeip = "";

    //获取wifi信息
    WifiManager my_wifiManager;
    private DhcpInfo dhcpInfo;
    private WifiInfo wifiInfo;
    byte[] data;
    LocalService myservice;
    private String username;

    String filenamestr="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_progress);
        ButterKnife.bind(this);
        my_wifiManager = ((WifiManager) getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = my_wifiManager.getDhcpInfo();
        wifiInfo = my_wifiManager.getConnectionInfo();
        Toolbar toolbar = getToolbar2();
        if (toolbar != null) {
            //改变标题
            toolbar.setTitle("文件发送");
            //改变标题栏背景

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SendProgress.this.finish();
                }
            });
        }
        String filepath=(String)SharePreferenceSetting.getSp("usertemp", "filepath", SendProgress.this,  new TypeToken<String>() {
        }.getType());
        File file=new File(filepath);
        if(file!=null)
        filenamestr=file.getName();
        sentoBoardCastinvite();
        cancelendboardcast.setText("取消");

        cancelendboardcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (issending) {
                    docancelsendbroad();
                    cancelendboardcast.setText("开始发送邀请");
                } else {
                    sentoBoardCastinvite();
                    cancelendboardcast.setText("取消");
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_progress, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        docancelsendbroad();


    }

    @Override
    protected void onDestroy() {
        unbingservice();
        docancelsendbroad();
        super.onDestroy();

    }

    /**
     * 取消广播发送的UI操作
     */
    private void cancelsendtips()
    {
        cancelendboardcast.setText("开始发送邀请");
        sendboardcastto.setText("点击按钮发送邀请！");
    }

    /**
     * 开始广播发送的UI操作
     */
    private void startsendtips()
    {
        cancelendboardcast.setText("取消");
        sendboardcastto.setText("向同一wifi网络设备发出连接邀请");
    }



    /**
     * 取消发广播
     */
    public void docancelsendbroad() {
        if (td != null) {
            td.interrupt();
        }


        issending = false;
        server = false;
        client = false;
        cancelsendtips();

    }

    /**
     * 初始化ip参数
     *
     * @param paramInt
     * @return
     */
    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }

    /**
     * 发送广播邀请
     */
    public void sentoBoardCastinvite() {
        docancelsendbroad();
        startsendtips();
        try {
            issending=true;
            int broadcast = dhcpInfo.ipAddress
                    | (~dhcpInfo.netmask);
            BROADCAST_IP = intToIp(broadcast);
            ip = intToIp(dhcpInfo.ipAddress);

            inetAddress = InetAddress.getByName(BROADCAST_IP);
            multicastSocket = new MulticastSocket(BROADCAST_PORT);
            multicastSocket.setTimeToLive(1);
            multicastSocket.joinGroup(inetAddress);
        // data = new byte[20 * 1024];

        } catch (Exception e) {
            e.printStackTrace();

        }
         connection();
        td = null;
        td = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                DatagramPacket dataPacket = null;
                // 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的

                db = new Datapacket();
                db.setCommand("link");
                db.setIp(ip);
              String  usernamex = (String) SharePreferenceSetting.getSp("Userinfo", "username", SendProgress.this, new TypeToken<String>(){}.getType());
                db.setHostname(usernamex);
                data = ObjectToByte(db);

               // 测试用  Datapacket ptest=(Datapacket) ByteToObject(data);

                dataPacket = new DatagramPacket(data, data.length,
                        inetAddress, BROADCAST_PORT);

                while (!Thread.currentThread().isInterrupted()) {
                    if (isRuning) {
                        try {
                            multicastSocket.send(dataPacket);
                            Thread.sleep(1000);
                            Log.i("broadcast", "发送广播ip...");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); //再次中断线程
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        });
        td.start();
    }




 Handler handler=new Handler()
    {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Datapacket packet2= (Datapacket) msg.obj;

                    String str2="";
                    if(packet2!=null)
                    {
                        str2=packet2.getCommand();
                    }

                    Toast.makeText(SendProgress.this, str2, Toast.LENGTH_SHORT).show();
                    sendboardcastto.setText(str2);

                    break;
                case 2:
                   /// sendmsg.setEnabled(true);
                    Datapacket packet= (Datapacket) msg.obj;

                    String str="";
                    if(packet!=null)
                    {
                       String ip=packet.getIp();
                   // String ip=str.substring(1, str.length());
                        docancelsendbroad();
                    removeip=ip;
                    Toast.makeText(SendProgress.this, "成功连接到用户："+packet.getHostname(), Toast.LENGTH_SHORT).show();
                    sendboardcastto.setText("已连接到用户:" + packet.getHostname());
                        new SendDataThread(removeip,"filemsg",SendProgress.this).start();

                        dialog();

                    }



                    break;
                default:
                    break;
            }
        }

    };


    Handler handler2=new Handler()
    {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                      int code= (int) msg.obj;
                    if(code==400)
                     Toast.makeText(SendProgress.this,"发送出现异常",Toast.LENGTH_SHORT).show();
                    break;
                case 2:

                    int percent= (int) msg.obj;
                    progressBar2.setProgress(percent);
                    progresstext.setText("文件："+filenamestr+"\n"+"已经发送了"+percent+"%");
                    break;
                default:
                    break;
            }
        }

    };



    private ServiceConnection sc=new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("aaa", "绑定成功");
            LocalService.LocalBinder binder = (LocalService.LocalBinder)service; //通过IBinder获取Service
            myservice=binder.getService();
            myservice.startWaitDataThread(handler);//完成绑定后打开另外一条线程等待消息接收
        }
    };

    /**
     * 绑定service
     */
    private void connection() {
        Intent intent = new Intent(this,LocalService.class);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }


    /**
     * 字节数组转对象
     * @param bytes
     * @return
     */
    private static java.lang.Object ByteToObject(byte[] bytes){

        java.lang.Object obj=null;
        try {
            //bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        }
        catch(Exception e) {
            System.out.println("translation"+e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 对象转字节数组
     * @param obj
     * @return
     */
    public byte[] ObjectToByte(java.lang.Object obj)
    {
        byte[] bytes=null;
        try {
            //object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        }
        catch(Exception e) {
            System.out.println("translation"+e.getMessage());
            e.printStackTrace();
        }
        return(bytes);
    }

    protected void dialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SendProgress.this);
        builder.setMessage("确认发送文件？");
         builder.setTitle("提示");

         builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 //Main.this.finish();
                 String filepath=(String)SharePreferenceSetting.getSp("usertemp", "filepath", SendProgress.this,  new TypeToken<String>() {
                 }.getType());
                 if(filepath!=null) {
                     File file = new File(filepath);
                     if (file.exists() && file.isFile()) {
                         new SendDataThread(removeip, SendProgress.this, handler2, file).start();
                     } else {
                         Toast.makeText(SendProgress.this, "文件不存在!", Toast.LENGTH_SHORT).show();
                     }
                 }



             }
         });
         builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){


             @Override
             public void onClick(DialogInterface dialog, int i) {
                 dialog.dismiss();
             }
         });
         builder.create().show();
        }



    /**
     * 取消绑定
     */
    private void unbingservice()
    {

        unbindService(sc);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
