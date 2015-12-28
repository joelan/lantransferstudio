package com.example.networkbroard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.bean.Datapacket;
import com.test.service.LocalService;
import com.test.threads.SendDataThread;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReceiveProgress extends BaseActivity {

    LocalService myservice;
    @Bind(R.id.location)
    TextView location;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.jindu)
    TextView jindu;
    private String Hostip;

    String filename;
    long filesize=1;

    public static final File FILE_SDCARD = Environment
            .getExternalStorageDirectory();
    public static final String SD_PATH = "Networktransfer";
    public static final File FILE_LOCAL = new File(FILE_SDCARD,
            SD_PATH);
    public static final File IMAGE_PATH_REIVE = new File(FILE_LOCAL,
            "images/receive");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_progress);
        ButterKnife.bind(this);

        Hostip=getIntent().getStringExtra("hostip");
        jindu.setText("send content succed");


        new SendDataThread(Hostip,"succed",this).start();
        Toolbar toolbar = getToolbar2();
        if (toolbar != null) {
            //改变标题
            toolbar.setTitle("文件接收");
            //改变标题栏背景

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ReceiveProgress.this.finish();
                }
            });
        }

        connection();

    }

    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("aaa", "绑定成功");
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service; //通过IBinder获取Service
            myservice = binder.getService();
            myservice.startWaitDataThread(handler);//完成绑定后打开另外一条线程等待消息接收
        }
    };

    Handler handler = new Handler() {

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


                    Toast.makeText(ReceiveProgress.this, str2, Toast.LENGTH_SHORT).show();
                    jindu.setText(str2);

                    break;
                case 2:
                    Datapacket packet= (Datapacket) msg.obj;

                    String str="";
                    if(packet!=null)
                    {
                        str=packet.getCommand();
                    }


                    Toast.makeText(ReceiveProgress.this, str, Toast.LENGTH_SHORT).show();
                    jindu.setText(str);
                    break;
                case 3:
                    Datapacket packet3= (Datapacket) msg.obj;
                      Log.i("handler","option is 3");
                    String str3="";
                    if(packet3!=null)
                    {
                        str3=packet3.getCommand();
                        if(myservice!=null&&str3.equals("filemsg"))
                        {

                            Log.i("handler","do is in");
                            if(!IMAGE_PATH_REIVE.exists())
                            {
                                IMAGE_PATH_REIVE.mkdirs();
                            }
                            if(!TextUtils.isEmpty(packet3.getFilename())) {
                                filename = IMAGE_PATH_REIVE.getAbsolutePath() + "/" + packet3.getFilename();
                                filesize=packet3.getFilesize();
                                location.setText("文件位置："+filename);
                                myservice.startReceiveFile(handler,filename);
                            }


                        }


                    }
                    else
                    {
                        Log.i("handler","packet3 is null");
                    }



                    Toast.makeText(ReceiveProgress.this, str3, Toast.LENGTH_SHORT).show();
                    jindu.setText(str3);
                    break;
                case 4:
//
                    long lenth= (long) msg.obj;

                    int percent= (int) (((float)lenth/(float)filesize)*100f);
                    progressBar.setProgress(percent);
                    Log.i("progress", percent+"%");
                    jindu.setText("已经接收："+percent+"%");

                    break;



                default:
                    break;
            }
        }

    };


    private void connection() {
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receive_progress, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
