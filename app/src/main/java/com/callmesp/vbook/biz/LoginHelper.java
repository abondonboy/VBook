package com.callmesp.vbook.biz;


import com.callmesp.vbook.api.MainApi;
import com.callmesp.vbook.base.JsonUtils;
import com.callmesp.vbook.base.LogUtil;
import com.callmesp.vbook.presenter.LoginPresenter;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Administrator on 2017/4/16.
 */

public class LoginHelper {
    private Retrofit retrofit;
    private MainApi registerApi;
    private LoginPresenter loginPresenter;
    public LoginHelper(LoginPresenter presenter) {
        loginPresenter=presenter;
        retrofit=new Retrofit.Builder()
                .baseUrl("http://111.231.134.55:4000/")
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        registerApi=retrofit.create(MainApi.class);
    }
    public void sendReqToGetEmail(String address){
        //String s="{ \"address\":" +"\""+address+"\"}";
        String s= JsonUtils.Builder()
                .addItem("address",address)
                .build();
        LogUtil.log("address",address);
        RequestBody requestBody=RequestBody.create(MediaType.parse("application/json"),s);
        registerApi.getEmail("getEmail",requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String res=responseBody.string();
                            if (res.equals("fail")){
                                loginPresenter.makeToast("该邮箱已被注册请直接登陆");
                            }else {
                                loginPresenter.makeToast("邮件已发送注意查收");
                            }
                            LogUtil.log("getemailresponse",": "+res);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.log("loginhelper:",e.toString());
                    }

                    @Override
                    public void onComplete() {
                        LogUtil.log("rxjava","getemailcomplete");
                    }
                });
    }
    public void sendReqToRegister(String address, String username, String pwd, int num){
        loginPresenter.showBar();
        String s="{ \"address\":" +"\""+address+"\","+
                "\"pwd\":"+"\""+pwd+"\","+
                "\"num\":"+num+","+
                "\"username\":\""+username+"\""+
                "}";
        final RequestBody requestBody=RequestBody.create(MediaType.parse("application/json"),s);
        registerApi.toRegister("register",requestBody)
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String res=responseBody.string();
                            if (res.equals("registersuccess")){
                                loginPresenter.makeToast("注册成功 ");
                            }
                            loginPresenter.hideBar();
                            LogUtil.log("registerresponse",": "+res);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.log("register",e.toString());
                    }

                    @Override
                    public void onComplete() {
                        LogUtil.log("rxjava","registercomplete");
                    }
                });
    }
    public void sendReqToLogin(String address, String pwd){
        String s="{ \"address\":" +"\""+address+"\","+
                "\"pwd\":"+"\""+pwd+"\""+
                "}";
        RequestBody requestBody=RequestBody.create(MediaType.parse("application/json"),s);
        registerApi.toLogin("login",requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody s) {
                        try {
                            String res=s.string();
                            LogUtil.log("login",""+res);
                            if (res.equals("loginFail")){
                                loginPresenter.loginFail();
                            }else{
                                loginPresenter.loginSuc(res);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.log("login:","error");
                    }

                    @Override
                    public void onComplete() {
                        LogUtil.log("rxjava","complete");
                    }
                });
    }
}
