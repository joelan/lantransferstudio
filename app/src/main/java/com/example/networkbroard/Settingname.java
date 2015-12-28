package com.example.networkbroard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.toolbarproject.BaseActivity;
import com.example.networkbroard.utils.SharePreferenceSetting;
import com.google.gson.reflect.TypeToken;

public class Settingname extends BaseActivity {

    @butterknife.Bind(R.id.textView1)
    TextView Tips;
    @butterknife.Bind(R.id.editText1)
    EditText username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingname);
        butterknife.ButterKnife.bind(this);


        Toolbar toolbar=getToolbar2();
        if(toolbar!=null)
        {
            //改变标题
            toolbar.setTitle("局域网传输");
            //改变标题栏背景
           // toolbar.setBackgroundColor(getResources().getColor(R.color.app_blue));
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
      /*  post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




            }
        });
*/
        String usernamex = (String) SharePreferenceSetting.getSp("Userinfo", "username", this, new TypeToken<String>() {
        }.getType());
          if(!TextUtils.isEmpty(usernamex))
          {
              //String usernamestr=username.getText().toString().trim();
             // SharePreferenceSetting.setSp("Userinfo", "username", Settingname.this, usernamestr);

              Intent intent=new Intent(Settingname.this,Main2Activity.class);
              startActivity(intent);
              Settingname.this.finish();
          }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settingmenua, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.savename) {
            if(!username.getText().toString().trim().equals("")&&username.getText().toString().trim().length()>1)
            {

                String usernamestr=username.getText().toString().trim();
                SharePreferenceSetting.setSp("Userinfo", "username", Settingname.this, usernamestr);
                Settingname.this.finish();
                Intent intent=new Intent(Settingname.this,Main2Activity.class);
                startActivity(intent);

            }
            else
            {
                Toast.makeText(Settingname.this, "请输入大于两个字符", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
