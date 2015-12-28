package com.example.networkbroard;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.bean.Datapacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Iphostlist extends BaseActivity {

    @Bind(R.id.Tips)
    TextView Tips;
    @Bind(R.id.RemoveHostlist)
    ListView RemoveHostlist;

    HashSet<Datapacket> hostset=new  HashSet<Datapacket>();
    List<HashMap<String,Object>> data = new ArrayList<HashMap<String,Object>>();
    SimpleAdapter adapter;
    /**
     * 客户端
     */
    private MulticastSocket multicastSocketclient=null;
    //BROADCAST_PORTCLIENT 跟服务端广播出去的端口号要一致，接收广播
    private static int BROADCAST_PORTCLIENT=9898;
    private static String BROADCAST_IPCLIENT="224.0.0.1";
    InetAddress inetAddressclient=null;
    Thread threadclient=null;
    private static String ipclientfuwu;

    //接收广播的heandler
    Handler myHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if(msg.what==1)
            {

                //tvResult.append("接收到服务器ip:"+msg.obj.toString()+"\n");
                Datapacket datap= (Datapacket) msg.obj;
                hostset.add(datap);

                Iterator it =hostset.iterator();
                data.clear();
                while(it.hasNext()){
                    // sop(it.next());
                    HashMap<String,Object> map = new HashMap<String,Object>();
                    Datapacket hostobject=(Datapacket) it.next();
                    if(hostobject!=null) {
                        map.put("host", hostobject.getIp());
                        map.put("hostname", hostobject.getHostname());
                      //  Log.i("receive",hostobject.getHostname());
                    }

                    data.add(map);

                }
                adapter.notifyDataSetChanged();


            }
        }
    };
    WifiManager my_wifiManager;
    private DhcpInfo dhcpInfo;
    private WifiInfo wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iphostlist);
        ButterKnife.bind(this);
        my_wifiManager = ((WifiManager) getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = my_wifiManager.getDhcpInfo();
        wifiInfo = my_wifiManager.getConnectionInfo();
        Toolbar toolbar = getToolbar2();
        if (toolbar != null) {
            //改变标题
            toolbar.setTitle("局域网传输");
            //改变标题栏背景

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Iphostlist.this.finish();
                }
            });
        }

        adapter=new SimpleAdapter(this, data, R.layout.listiteminfoofhost, new String[]{"host","hostname"}, new int[]{R.id.serverhost,R.id.hostname});
        RemoveHostlist.setAdapter(adapter);
        RemoveHostlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //hostip
                Intent intent = new Intent(Iphostlist.this, ReceiveProgress.class);
                String hostip = (String) data.get(position).get("host");
                intent.putExtra("hostip", hostip);
                startActivity(intent);
            }
        });

        StartReceive();

        Tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(threadclient!=null)
                {
                    if(threadclient.isInterrupted())
                    {

                        StartReceive();
                    }

                }
                else
                {
                    StartReceive();
                }

            }
        });

    }


    private void startreceivetips()
    {

        Tips.setText("正在发现附近设备...");

    }

    private  void canlcecevietips()
    {

        Tips.setText("点击开启发现设备...");

    }
    /**
     * 开启接收模式
     */
    public void StartReceive()
    {
        startreceivetips();
        try
        {
            int broadcast = dhcpInfo.ipAddress
                    | (~dhcpInfo.netmask);
            BROADCAST_IPCLIENT = intToIp(broadcast);
            multicastSocketclient = new MulticastSocket(BROADCAST_PORTCLIENT);
            inetAddressclient= InetAddress.getByName(BROADCAST_IPCLIENT);
            multicastSocketclient.joinGroup(inetAddressclient);


        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
        threadclient=null;
        threadclient=new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        byte buf[] = new byte[100*1024];
                        DatagramPacket dp =null;

                        dp=new DatagramPacket(buf,buf.length,inetAddressclient,BROADCAST_PORTCLIENT);

                        Message msg=new Message();
                        msg.what=1;
                        multicastSocketclient.receive(dp);

                        Object obj=ByteToObject(dp.getData());

                        Thread.sleep(1000);

                        Datapacket packet = (Datapacket) obj;
                        msg.obj=packet;

                        ipclientfuwu= packet.getIp();
                        myHandler.sendMessage(msg);

                        Log.i("receive", "检测到服务端IP : " + packet.getIp());
                    }
                    catch(InterruptedException e)
                    {

                        Thread.currentThread().interrupt(); //再次中断线程
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }


            }
        });


        threadclient.start();


    }



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
            e.printStackTrace();
        }
        return(bytes);
    }


    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }

    @Override
    protected void onPause() {
        super.onPause();
        docanclereceivebroad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_iphostlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 取消接收
     */
    public void docanclereceivebroad()
    {
        if (threadclient != null) {
            threadclient.interrupt();
        }
        canlcecevietips();

    }

}
