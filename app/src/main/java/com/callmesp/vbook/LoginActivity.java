package com.callmesp.vbook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.callmesp.vbook.IListener.ILoginActivity;
import com.callmesp.vbook.base.LogUtil;
import com.callmesp.vbook.presenter.LoginPresenter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,ILoginActivity{

    private LoginPresenter loginPresenter=new LoginPresenter(this);

    @BindView(R.id.edit_email)TextView email_text;
    @BindView(R.id.edit_password)TextView pwd_text;
    @BindView(R.id.btn_login)Button login_btn;
    @BindView(R.id.btn_register)Button register_btn;
    @BindView(R.id.login_progress)ProgressBar progressBar;
    @BindView(R.id.btn_getemail)Button getemail_btn;
    @BindView(R.id.edit_checknum)EditText checknum_text;
    @BindView(R.id.linear_register)LinearLayout linearLayout;
    @BindView(R.id.edit_username)EditText username_text;

    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        login_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        getemail_btn.setOnClickListener(this);

        username_text.setText("callsp");
        email_text.setText("995199235@qq.com");
        pwd_text.setText("sp123456");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_register:
                if (linearLayout.getVisibility()==View.GONE){
                    linearLayout.setVisibility(View.VISIBLE);
                    login_btn.setVisibility(View.GONE);
                    register_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    register_btn.setTextColor(getResources().getColor(R.color.white));
                }else {
                    //提交注册申请的地方
                    String address=email_text.getText().toString();
                    String pwd=pwd_text.getText().toString();
                    String check=checknum_text.getText().toString();
                    String username=username_text.getText().toString();
                    int num=Integer.valueOf(check);
                    //提交注册申请
                    doRegister(address,username,pwd,num);
                    //UI恢复
                    linearLayout.setVisibility(View.GONE);
                    register_btn.setBackgroundColor(getResources().getColor(R.color.gray));
                    register_btn.setTextColor(getResources().getColor(R.color.white));
                    login_btn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_login:
                String address=email_text.getText().toString();
                String pwd=pwd_text.getText().toString();
                doLogin(address,pwd);
                break;
            case R.id.btn_getemail:
                String emailaddress=email_text.getText().toString();
                doGetEmail(emailaddress);
                break;
        }
    }
    private void doGetEmail(String address){
        loginPresenter.getEmail(address);
    }
    private void doRegister(String address,String username,String pwd,int check){
        loginPresenter.register(address,username,pwd,check);
    }
    private void doLogin(String address,String pwd){
        loginPresenter.login(address,pwd);
    }

    @Override
    public void hideProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this,str,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void loginFail() {
        hideProgressBar();
        makeToast("密码错误 ");
    }

    @Override
    public void loginSuccess(String uuid) {
        hideProgressBar();
        makeToast("登陆成功");
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("uuid",uuid);
        startActivity(intent);
    }

}
