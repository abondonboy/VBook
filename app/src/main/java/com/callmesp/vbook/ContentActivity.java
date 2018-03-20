package com.callmesp.vbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.callmesp.vbook.api.MainApi;
import com.callmesp.vbook.base.JsonUtils;
import com.callmesp.vbook.base.LogUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
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
 * Created by Administrator on 2018/1/17.
 */

public class ContentActivity extends AppCompatActivity {
    @BindView(R.id.txt_content)TextView textView;
    @BindView(R.id.btn_order)Button button;
    private Retrofit retrofit;
    private MainApi mainApi;
    private RequestBody requestBody;
    private static final String TAG = "ContentActivity";
    private String uuid;

    String name,address,tel,recipname,recipadd,reciptel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        init();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result= JsonUtils.Builder()
                        .addItem("uuid",uuid)
                        .addItem("name",name)
                        .addItem("address",address)
                        .addItem("tel",tel)
                        .addItem("recipname",recipname)
                        .addItem("recipadd",recipadd)
                        .addItem("reciptel",reciptel)
                        .build();
                requestBody=RequestBody.create(MediaType.parse("application/json"),result);
                upLoad();
            }
        });
    }

    private void init(){
        ButterKnife.bind(this);

        Intent intent=getIntent();
        String content=intent.getStringExtra("content");
        name=intent.getStringExtra("name");
        address=intent.getStringExtra("address");
        tel=intent.getStringExtra("tel");
        recipname=intent.getStringExtra("recipname");
        recipadd=intent.getStringExtra("recipadd");
        reciptel=intent.getStringExtra("reciptel");
        uuid=intent.getStringExtra("uuid");

        retrofit=new Retrofit.Builder()
                .baseUrl("http://111.231.134.55:4000/")
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mainApi=retrofit.create(MainApi.class);

        textView.setText(content);

    }

    private void upLoad(){
        mainApi.postOrder("uploadOrder",requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            if (responseBody.string().equals("suc")){
                                Toast.makeText(ContentActivity.this,"下单成功",Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Toast.makeText(ContentActivity.this,"下单失败",Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.log(TAG,"onerror");
                    }

                    @Override
                    public void onComplete() {

                        LogUtil.log(TAG,"complete");
                    }
                });
    }
}
