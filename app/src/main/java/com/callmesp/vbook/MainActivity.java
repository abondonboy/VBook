package com.callmesp.vbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.callmesp.vbook.biz.UiMessageListener;
import com.callmesp.vbook.base.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.*;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2018/1/9.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,EventListener{

    @BindView(R.id.txt_name)EditText editText_name;
    @BindView(R.id.btn_voice_name)Button button_name;

    @BindView(R.id.txt_address)EditText editText_address;
    @BindView(R.id.btn_voice_address)Button button_address;

    @BindView(R.id.txt_tel)EditText editText_tel;
    @BindView(R.id.btn_voice_tel)Button button_tel;

    @BindView(R.id.txt_recipient)EditText editText_recipName;
    @BindView(R.id.btn_voice_recipient)Button button_recipName;

    @BindView(R.id.txt_recipAddress)EditText editText_recipAddress;
    @BindView(R.id.btn_voice_recipAddress)Button button_recipAddress;

    @BindView(R.id.txt_recipTel)EditText editText_recipTel;
    @BindView(R.id.btn_voice_recipTel)Button button_recipTel;

    @BindView(R.id.tradition_affirm)Button button_tradition;
    @BindView(R.id.voice_affirm)Button button_voice;

    //语音识别初始化
    private EventManager asr;
    //语音合成初始化
    protected SpeechSynthesizer mSpeechSynthesizer;
    //语音合成结果
    private String result="";
    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;
    //用于处理语音识别回调
    protected Handler mainHandler;

    private static final String TAG = "MainActivity";
    private String READTEXT="这是一个测试";;

    private final int   NAME=1,
                        ADDRESS=2,
                        TEL=3,
                        RECIP_NAME=4,
                        RECIP_ADDRESS=5,
                        RECIP_TEL=6;

    private int CURRENT_STATE=0;

    protected String appId = "10250719";

    protected String appKey = "bUvzFxRyelDpAVDs7PAUGxjC";

    protected String secretKey = "eb664a8bfbaddf0c8acba65a9493e44b";

    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        init();

        LogUtil.log(TAG,editText_name.getText().toString());
        
    }

    private void init(){
        ButterKnife.bind(this);
        initPermission();
        asr= EventManagerFactory.create(this,"asr");
        asr.registerListener(this);

        new Thread(){
            @Override
            public void run() {
                initTTs();
            }
        }.start();

        button_name.setOnClickListener(this);
        button_address.setOnClickListener(this);
        button_tel.setOnClickListener(this);
        button_recipName.setOnClickListener(this);
        button_recipAddress.setOnClickListener(this);
        button_recipTel.setOnClickListener(this);
        button_tradition.setOnClickListener(this);
        button_voice.setOnClickListener(this);

        Intent intent=getIntent();
        uuid=intent.getStringExtra("uuid");

        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj != null) {
                    LogUtil.log("mainhandler:",msg.obj.toString());
                }
            }

        };
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    private void start() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params_json.put(SpeechConstant.NLU, "enable");
        // params_json.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params_json.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params_json.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params_json.put(SpeechConstant.PROP ,20000);
        // params_json.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params_json);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
    }

    private void stop() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }

    //   EventListener  回调方法
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;


        if (params != null && !params.isEmpty()) {
            logTxt += " ;params_json :" + params;
            String recog="";
            try {
                recog=new JSONObject(params).getString("results_recognition").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (recog.length()>=result.length()) {
                Log.e("main","recog:"+recog);

                String pattern="\"(.*?)\"";
                Pattern pattern1=Pattern.compile(pattern);
                Matcher matcher=pattern1.matcher(recog);
                while (matcher.find()) {
                    result=matcher.group(1);
                    LogUtil.log(TAG,"result:"+result);
                }

                switch (CURRENT_STATE){
                    case NAME:
                        editText_name.setText(result);
                        break;
                    case ADDRESS:
                        editText_address.setText(result);
                        break;
                    case TEL:
                        editText_tel.setText(result);
                        break;
                    case RECIP_NAME:
                        editText_recipName.setText(result);
                        break;
                    case RECIP_ADDRESS:
                        editText_recipAddress.setText(result);
                        break;
                    case RECIP_TEL:
                        editText_recipTel.setText(result);
                        break;
                }

            }
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)){
            Log.e("main","finish");
            stop();
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params.contains("\"nlu_result\"")) {
                if (length > 0 && data.length > 0) {
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            }
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_voice_name:
                CURRENT_STATE=NAME;
                result="";
                start();
                break;
            case R.id.btn_voice_address:
                CURRENT_STATE=ADDRESS;
                result="";
                start();
                break;
            case R.id.btn_voice_tel:
                CURRENT_STATE=TEL;
                result="";
                start();
                break;
            case R.id.btn_voice_recipient:
                CURRENT_STATE=RECIP_NAME;
                result="";
                start();
                break;
            case R.id.btn_voice_recipAddress:
                CURRENT_STATE=RECIP_ADDRESS;
                result="";
                start();
                break;
            case R.id.btn_voice_recipTel:
                CURRENT_STATE=RECIP_TEL;
                result="";
                start();
                break;
            case R.id.voice_affirm:
                read();
                break;
            case R.id.tradition_affirm:
                show();
                break;
        }
    }

    private void show(){
        READTEXT="";
        if (editText_name.getText().toString().length()>0&&
                editText_tel.getText().toString().length()>0&&
                editText_address.getText().toString().length()>0&&
                editText_recipName.getText().toString().length()>0&&
                editText_recipTel.getText().toString().length()>0&&
                editText_recipAddress.getText().toString().length()>0) {
            READTEXT+="您的姓名为："+editText_name.getText().toString()+"。\n"+
                    "您的地址是："+editText_address.getText().toString()+"。\n"+
                    "您的电话是："+editText_tel.getText().toString()+"。\n"+
                    "收件人姓名是："+editText_recipName.getText().toString()+"。\n"+
                    "收件人地址是："+editText_recipAddress.getText().toString()+"。\n"+
                    "收件人电话是："+editText_recipTel.getText().toString()+"。\n";
            Intent intent=new Intent(this,ContentActivity.class);
            intent.putExtra("name",editText_name.getText().toString());
            intent.putExtra("address",editText_address.getText().toString());
            intent.putExtra("tel",editText_tel.getText().toString());
            intent.putExtra("recipname",editText_recipName.getText().toString());
            intent.putExtra("recipadd",editText_recipAddress.getText().toString());
            intent.putExtra("reciptel",editText_recipTel.getText().toString());
            intent.putExtra("content",READTEXT);
            intent.putExtra("uuid",uuid);
            startActivity(intent);
        } else {
            Toast.makeText(this,"内容不完整请继续输入",Toast.LENGTH_SHORT).show();
        }
    }

    private void read(){
        LogUtil.log(TAG,editText_name.getText().toString());
        if (editText_name.getText().toString().length()>0&&
                editText_tel.getText().toString().length()>0&&
                editText_address.getText().toString().length()>0&&
                editText_recipName.getText().toString().length()>0&&
                editText_recipTel.getText().toString().length()>0&&
                editText_recipAddress.getText().toString().length()>0){
            READTEXT="";
            READTEXT+="您的姓名为："+editText_name.getText().toString()+"。"+
                    "您的地址是："+editText_address.getText().toString()+"。"+
                    "您的电话是："+editText_tel.getText().toString()+"。"+
                    "收件人姓名是："+editText_recipName.getText().toString()+"。"+
                    "收件人地址是："+editText_recipAddress.getText().toString()+"。"+
                    "收件人电话是："+editText_recipTel.getText().toString()+"。";
        }else {
            READTEXT="内容输入不完整请继续输入";
        }
        speak();

    }
    private void initTTs() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;

        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

        // 3. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        //checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        //checkResult(result, "setApiKey");

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // 5. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        //checkResult(result, "initTts");

    }
    private void speak() {
        /* 以下参数每次合成时都可以修改
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
         *  设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
         *
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
         *  MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         */

        if (mSpeechSynthesizer == null) {
            LogUtil.log("MainActivity_speak","[ERROR], 初始化失败");
            return;
        }
        mSpeechSynthesizer.speak(READTEXT);
    }

    private void readStop() {
        mSpeechSynthesizer.stop();
    }

}
