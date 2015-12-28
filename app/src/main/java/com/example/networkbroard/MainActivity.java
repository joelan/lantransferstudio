package com.example.networkbroard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.bean.Datapacket;
import com.test.service.LocalService;
import com.test.service.LocalService.LocalBinder;
import com.test.threads.SendDataThread;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends BaseActivity {

	private WifiManager my_wifiManager;
	private DhcpInfo dhcpInfo;
	private WifiInfo wifiInfo;
	private TextView tvResult;

	Boolean send=true;
	Boolean receive=true;



	/**
	 * 服务端
	 */
	//udp
	boolean client=true;
	boolean server = false;
	private static String ip; // 服务端ip
	private static int BROADCAST_PORT = 9898;
	private static String BROADCAST_IP = "224.0.0.1";
	InetAddress inetAddress = null;
	Thread t = null;
	/* 发送广播端的socket */
	MulticastSocket multicastSocket = null;
	/* 发送广播的按钮 */
	private Button sendUDPBrocast;
	Button ReceiveUDPbrocast;
	private volatile boolean isRuning = true;
	TextView ipInfo;
	Thread td;
	boolean issending = false;
	boolean isreceiceing=false;


	Button sendmsg;
	String removeip="";

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
	//TextView ipInfo;
	ListView  hostlist ;

	LocalService myservice;



	//接收广播的heandler
	Handler  myHandler=new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if(msg.what==1)
			{

				//tvResult.append("接收到服务器ip:"+msg.obj.toString()+"\n");
				hostset.add(msg.obj.toString());

				Iterator it =hostset.iterator();
				data.clear();
				while(it.hasNext()){
					// sop(it.next());
					HashMap<String,Object>map = new HashMap<String,Object>();
					String host=(String) it.next();
					map.put("host", host);

					data.add(map);

				}
				adapter.notifyDataSetChanged();


			}
		}
	};

	Handler handler=new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:

					Datapacket packet= (Datapacket) msg.obj;

					String str="";
					if(packet!=null)
					{
						str=packet.getCommand();
					}
					Toast.makeText(MainActivity.this, str,  Toast.LENGTH_SHORT).show();
					tvResult.setText(str);

					break;
				case 2:
					sendmsg.setEnabled(true);
					Datapacket packet2= (Datapacket) msg.obj;

					String str2="";
					if(packet2!=null)
					{
						str2=packet2.getCommand();
					}

					String ip=str2.substring(1,str2.length());
					removeip=ip;
					Toast.makeText(MainActivity.this, "MainActivityremoveip:"+removeip, Toast.LENGTH_SHORT).show();
					tvResult.setText("removeip:"+removeip);
					docanclereceivebroad();
					docancelsendbroad();

					break;
				default:
					break;
			}
		}

	};


	HashSet<String> hostset=new  HashSet<String>();
	List<HashMap<String,Object>> data = new ArrayList<HashMap<String,Object>>();
	SimpleAdapter adapter;
	EditText sendata;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar=getToolbar2();
		if(toolbar!=null)
		{
			//改变标题
			toolbar.setTitle("局域网传输");
			//改变标题栏背景
			// toolbar.setBackgroundColor(getResources().getColor(R.color.app_blue));
			this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
		my_wifiManager = ((WifiManager) getSystemService(Context.WIFI_SERVICE));
		dhcpInfo = my_wifiManager.getDhcpInfo();
		wifiInfo = my_wifiManager.getConnectionInfo();
		sendUDPBrocast = (Button) findViewById(R.id.sendboardcast);
		tvResult = (TextView) findViewById(R.id.tvResult);
		ReceiveUDPbrocast=(Button)findViewById(R.id.receiveboardcast);
		sendUDPBrocast.setText("发送广播");
		ReceiveUDPbrocast.setText("接收广播");
		hostlist=(ListView)findViewById(R.id.hostlist);
		sendmsg=(Button)findViewById(R.id.send);
		sendata=(EditText)findViewById(R.id.senddata);
		sendmsg.setEnabled(false);

		if (isWifiConnected(this)) {
			sendUDPBrocast.setEnabled(true);
			ReceiveUDPbrocast.setEnabled(true);

		} else {
			sendUDPBrocast.setEnabled(false);
			ReceiveUDPbrocast.setEnabled(false);
		}

		InitBroadcastinfo();
		sendmsg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!sendata.getText().toString().trim().equals(""))
				{

					String data=sendata.getText().toString().trim();
					Log.i("mainActivity", data);
					new SendDataThread(removeip,data,MainActivity.this).start();
				}
				else
				{
					Toast.makeText(MainActivity.this, "请输入文字消息", Toast.LENGTH_SHORT).show();
				}

			}
		});


		adapter=new SimpleAdapter(this, data, R.layout.listitem, new String[]{"host"}, new int[]{R.id.serverhost});

		hostlist.setAdapter(adapter);
		hostlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(MainActivity.this,ConnectionActivity.class);
				intent.putExtra("hostip", data.get(position).get("host").toString());
				startActivity(intent);
				docanclereceivebroad();
				docancelsendbroad();


			}
		});


	}

	private ServiceConnection sc=new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("aaa", "绑定成功");
			LocalBinder binder = (LocalBinder)service; //通过IBinder获取Service  
			myservice=binder.getService();
			myservice.startWaitDataThread(handler);//完成绑定后打开另外一条线程等待消息接收
		}
	};


	private void connection() {
		Intent intent = new Intent(this,LocalService.class);
		bindService(intent, sc, this.BIND_AUTO_CREATE);
	}
	@Override
	public void onDestroy()
	{
		unbindService(sc);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		docancelsendbroad();
		docanclereceivebroad();
	}

	/**
	 * 取消发广播
	 */
	public void docancelsendbroad()
	{
		if (td != null) {
			td.interrupt();
		}
		sendUDPBrocast.setText("开始发送广播");
		ReceiveUDPbrocast.setEnabled(true);
		issending = false;
		server = false;
		client=false;
	}

	/**
	 *取消接收网络广播 
	 */
	public void docanclereceivebroad()
	{
		server = false;
		client=false;

		if (threadclient != null) {
			threadclient.interrupt();
//			threadclient.stop();
//			threadclient=null;



		}
		ReceiveUDPbrocast.setText("接收广播");
		//sendUDPBrocast.setText("开始广播");
		sendUDPBrocast.setEnabled(true);
		isreceiceing=false;
	}


	/**
	 * 初始化收发广播信息
	 */
	public void InitBroadcastinfo()
	{
		ReceiveUDPbrocast.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//sendUDPBrocast.setEnabled(false);
				if(!issending)
				{
					if(!isreceiceing)
					{



						isreceiceing=true;
						sendUDPBrocast.setEnabled(false);
						ReceiveUDPbrocast.setText("停止接收广播");
						server = false;
						client=true;
						try
						{
							int broadcast = dhcpInfo.ipAddress
									| (~dhcpInfo.netmask);
							BROADCAST_IPCLIENT = intToIp(broadcast);
							multicastSocketclient = new MulticastSocket(BROADCAST_PORTCLIENT);
							inetAddressclient=InetAddress.getByName(BROADCAST_IPCLIENT);
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


								byte buf[] = new byte[1024];
								DatagramPacket dp =null;
								//BROADCAST_PORTCLIENT，inetAddressclient 要监听的广播地址和广播端口
								dp=new DatagramPacket(buf,buf.length,inetAddressclient,BROADCAST_PORTCLIENT);




								while (!Thread.currentThread().isInterrupted())
								{
									try
									{
										Message msg=new Message();
										msg.what=1;
										multicastSocketclient.receive(dp);
										Thread.sleep(1000);
										ipclientfuwu=new String(buf, 0, dp.getLength());
										msg.obj=ipclientfuwu;
										myHandler.sendMessage(msg);
										Log.i("receive", "检测到服务端IP : "+ipclientfuwu);
										// System.out.println("检测到服务端IP : "+ipclientfuwu);
									}
									catch(InterruptedException e)
									{
										//   System.out.println("InterruptedException");
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
					else
					{
						docanclereceivebroad();
						/*server = false;
						client=false;
						
						if (threadclient != null) {
							threadclient.interrupt();

						
							
							
						}
						ReceiveUDPbrocast.setText("接收广播");
		
						sendUDPBrocast.setEnabled(true);
						isreceiceing=false;*/
					}

				}




			}
		});


		sendUDPBrocast.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(!isreceiceing)
				{
					if (!issending) {
						issending=true;

						server = true;
						client=false;

						sendUDPBrocast.setText("停止发送广播");
						ReceiveUDPbrocast.setEnabled(false);
						try {

							int broadcast = dhcpInfo.ipAddress
									| (~dhcpInfo.netmask);
							BROADCAST_IP = intToIp(broadcast);
							ip = intToIp(dhcpInfo.ipAddress);

							inetAddress = InetAddress.getByName(BROADCAST_IP);
							multicastSocket = new MulticastSocket(BROADCAST_PORT);
							multicastSocket.setTimeToLive(1);
							multicastSocket.joinGroup(inetAddress);

						} catch (Exception e) {
							e.printStackTrace();

						}
						connection();
						td=null;
						td = new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								DatagramPacket dataPacket = null;
								// 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的

								byte[] data = ip.getBytes();
								dataPacket = new DatagramPacket(data, data.length,
										inetAddress, BROADCAST_PORT);


								while (!Thread.currentThread().isInterrupted()) {
									if (isRuning) {
										try {
											multicastSocket.send(dataPacket);
											Thread.sleep(1000);
											Log.i("broadcast", "发送广播ip...");
										}
										catch(InterruptedException e)
										{
											//  System.out.println("InterruptedException");
											Thread.currentThread().interrupt(); //再次中断线程
										}

										catch (Exception e) {
											e.printStackTrace();
										}
									}
								}




							}
						});
						td.start();
					} else {

						docancelsendbroad();

					}

					//issending = true;
				}


			}
		});
	}


	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected()) {
			return true;
		}

		return false;
	}

	public void Initnetworkip() {

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		/*
		 * StringBuilder sb = new StringBuilder();
		 * 
		 * sb.append("网络信息："); sb.append("\nipAddress：" +
		 * intToIp(dhcpInfo.ipAddress)); sb.append("\nnetmask：" +
		 * intToIp(dhcpInfo.netmask)); int
		 * broadcast=dhcpInfo.ipAddress|(~dhcpInfo.netmask);
		 * sb.append("\n广播地址："+intToIp(broadcast)); sb.append("\ngateway：" +
		 * intToIp(dhcpInfo.gateway)); sb.append("\nserverAddress：" +
		 * intToIp(dhcpInfo.serverAddress)); sb.append("\ndns1：" +
		 * intToIp(dhcpInfo.dns1)); sb.append("\ndns2：" +
		 * intToIp(dhcpInfo.dns2)); sb.append("\n");
		 * 
		 * 
		 * sb.append("Wifi信息："); sb.append("\nIpAddress：" +
		 * intToIp(wifiInfo.getIpAddress())); sb.append("\nMacAddress：" +
		 * wifiInfo.getMacAddress()); tvResult.setText(sb.toString());
		 */

	}

	private String intToIp(int paramInt) {
		return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
				+ (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
