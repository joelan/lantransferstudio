package com.example.networkbroard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.utils.DisplayUtil;
import com.example.networkbroard.utils.FileUtils;
import com.example.networkbroard.utils.SharePreferenceSetting;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.reflect.Field;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Main2Activity extends BaseActivity implements View.OnClickListener {

    private static final int  FILE_SELECT_CODE =10;
    @Bind(R.id.wantSend)
    Button wantSend;
    @Bind(R.id.wantReceive)
    Button wantReceive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);

        wantSend.setOnClickListener(this);
        wantReceive.setOnClickListener(this);
        final Toolbar toolbar=getToolbar2();

        DisplayImageOptions	options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(100))
                .build();



        if(toolbar!=null)
        {
            //改变标题
            String usernamex = (String) SharePreferenceSetting.getSpString("Userinfo", "username", Main2Activity.this);

            Log.i("Main2Activity","do");
            if(usernamex!=null) {
                toolbar.setTitle("昵称："+usernamex);
               // toolbar.setSubtitle("昵称");

                Log.i("Main2Activity",usernamex);
            }
            else
            {
                Log.i("Main2Activity","usernamex is null");
            }
            //改变标题栏背景

            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
           // toolbar.setNavigationIcon(R.drawable.userhead);
            //toolbar.setLogo(R.drawable.userhead);
            Field fields= null;
            Field  titlefield=null;
            ImageView imageview=null;
            TextView title=null;
            try {
                fields = toolbar.getClass().getDeclaredField("mLogoView");
                titlefield=toolbar.getClass().getDeclaredField("mTitleTextView");
                if(fields!=null)
                {
                    fields.setAccessible(true);//修改访问权限
                    titlefield.setAccessible(true);
                    imageview= (ImageView) fields.get(toolbar);
                    title=(TextView)titlefield.get(toolbar);

                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                Log.i("Main2Actvity",e.toString());

            }
            catch (IllegalAccessException e) {
                Log.i("Main2Actvity", e.toString());
            }


              if(title!=null)
              {
               title.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Log.i("Main2Activity", "titleonclick");
                       showdialog();
                   }
               });
              }

          if(imageview!=null) {
              final ImageView logoview = imageview;
              Log.i("Main2Actvity","iamge is not null");
              logoview.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      showdialog();
                  }
              });
             // imageview.setVisibility(View.VISIBLE);
             // ImageLoader.getInstance().displayImage("drawable://" + R.drawable.userhead2 ,imageview,options);
          }
        else
          {
              Log.i("Main2Actvity", "iamge is null");
          }






            toolbar.setLogo(R.drawable.userhead2);


            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //  Main2Activity.this.finish();
                    Log.i("Main2Activity", "onclick");
                    showdialog();

                }
            });

//            toolbar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showdialog();
//                }
//            });

        }

        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");			//添加动态广播的Action
        registerReceiver(new receivernetwork(), dynamic_filter);


    }

    public void showdialog()
    {
        final EditText input = new EditText(this);    //定义一个EditText

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置昵称");
        // builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setView(input, DisplayUtil.dip2px(this,10),DisplayUtil.dip2px(this,10),DisplayUtil.dip2px(this,10),0);       //将EditText添加到builder中


        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!input.getText().toString().trim().equals("") && input.getText().toString().trim().length() > 1) {

                    String usernamestr = input.getText().toString().trim();
                    SharePreferenceSetting.setSp("Userinfo", "username", Main2Activity.this, usernamestr);
                    // Settingname.this.finish();

                    Toolbar toolbar = getToolbar2();
                    if (toolbar != null) {
                        toolbar.setTitle("昵称："+usernamestr);
                    }

                    dialog.dismiss();

                } else {
                    Toast.makeText(Main2Activity.this, "请输入大于两个字符", Toast.LENGTH_SHORT).show();
                   // builder.create().show();
                    dialog.dismiss();
                    showdialog();
                }


            }
        });
        builder.create().show();

    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "选择一个文件发送"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    SharePreferenceSetting.setSp("usertemp","filepath",this,path);
                    Intent intent=new Intent(Main2Activity.this,SendProgress.class);
                    startActivity(intent);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.wantSend:
                showFileChooser();
                break;
            case R.id.wantReceive:
                Intent intet=new Intent(Main2Activity.this,Iphostlist.class);
                startActivity(intet);

                break;
        }

    }

    public class receivernetwork extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(Main2Activity.this.isWifiConnected(context))
            {
                wantSend.setEnabled(true);
                wantReceive.setEnabled(true);
            }
            else
            {
                wantSend.setEnabled(false);
                wantReceive.setEnabled(false);
            }

        }

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

}
