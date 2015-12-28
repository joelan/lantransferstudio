package com.example.networkbroard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.bean.Datapacket;
import com.test.service.LocalService;
import com.test.service.LocalService.LocalBinder;
import com.test.threads.SendDataThread;

public class ConnectionActivity extends BaseActivity {


	EditText  senddata;
	Button send;
	TextView  serverhost;
	String Hostip;

	LocalService myservice;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection);
		send=(Button)findViewById(R.id.sendtoServer);
		senddata=(EditText)findViewById(R.id.senddata);
		serverhost=(TextView)findViewById(R.id.serverhost);
		Hostip=getIntent().getStringExtra("hostip");
		serverhost.setText(Hostip);


		new SendDataThread(Hostip,"succed",this).start();
		connection();

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(!senddata.getText().toString().trim().equals(""))
				{
					String data=senddata.getText().toString().trim();
					new SendDataThread(Hostip,data,ConnectionActivity.this).start();
				}
				else
				{
					Toast.makeText(ConnectionActivity.this, "请输入文字消息", Toast.LENGTH_SHORT).show();
				}

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
					Toast.makeText(ConnectionActivity.this, str, Toast.LENGTH_SHORT).show();
					serverhost.setText(str);

					break;
				case 2:

					Datapacket packet2= (Datapacket) msg.obj;
					String str2="";
					if(packet2!=null)
					{
						str2=packet2.getCommand();
					}
					Toast.makeText(ConnectionActivity.this, str2,Toast.LENGTH_SHORT).show();
					serverhost.setText(str2);
					break;
				default:
					break;
			}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connectionmenua, menu);
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
