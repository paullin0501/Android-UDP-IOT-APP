package com.rexlite.rexlitebasicnew.SceneShortcut;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.DeviceCommand;
import com.rexlite.rexlitebasicnew.MainActivity;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RxBus;
import com.rexlite.rexlitebasicnew.SceneSettingActivity;
import com.rexlite.rexlitebasicnew.UDP;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SceneSleepingModeActivity extends AppCompatActivity {
    private static final String TAG = "scenesleeping";
    byte[] maxSceneSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    DeviceCommand deviceCommand = new DeviceCommand();
    byte[] chData;//指令(還不包括亮度與SN)
    ExecutorService exec = Executors.newCachedThreadPool();
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(SceneSleepingModeActivity.this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_sleeping_mode);
        //將裝置的值保留
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        maxSceneSN = info.getByteArray("maxSceneSN");
        deviceName = info.getString("deviceName");
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        //返回按鍵設定
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SceneSleepingModeActivity.this, MoreActionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN", maxSceneSN);
                bundle.putString("deviceName", deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideRight(SceneSleepingModeActivity.this);
            }
        });
        //裝置名稱
        title.setText("Sleeping mode");

        //ok按鍵設定
        final Button ok_btn = findViewById(R.id.ok_button);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedText = null;
                String selectedText2 = null;
                String timeText = null;
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group_1);
                RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.radio_group_2);
                int radioButtonID2 = radioGroup2.getCheckedRadioButtonId();
                RadioButton radioButton2 = (RadioButton) radioGroup2.findViewById(radioButtonID2);
                RadioButton min_btn = (RadioButton) radioGroup2.findViewById(R.id.hr30_btn);
                RadioButton hour_btn = (RadioButton) radioGroup2.findViewById(R.id.hr1_btn);
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
                RadioButton bright_btn = (RadioButton) radioGroup.findViewById(R.id.bright_btn);
                RadioButton sleep_btn = (RadioButton) radioGroup.findViewById(R.id.sleep_btn);

                if (radioButton.getId() == sleep_btn.getId()) {
                    selectedText = "sleep";
                    if (radioButton2.getId()==min_btn.getId()){
                        selectedText2 = "0.5 hour";
                        timeText = "min sleep";
                        chData = settingSceneSleepingModeCMD(maxSceneSN,timeText);
                    }
                    if (radioButton2.getId()==hour_btn.getId()){
                        selectedText2 = "1 hour";
                        timeText = "hour sleep";
                        chData = settingSceneSleepingModeCMD(maxSceneSN,timeText);
                    }
                }
                if (radioButton.getId() == bright_btn.getId()) {
                    selectedText = "bright";
                    if (radioButton2.getId()==min_btn.getId()){
                        selectedText2 = "0.5 hour";
                        timeText = "min bright";
                        chData = settingSceneSleepingModeCMD(maxSceneSN,timeText);
                    }
                    if (radioButton2.getId()==hour_btn.getId()){
                        selectedText2 = "1 hour";
                        timeText = "hour bright";
                        chData = settingSceneSleepingModeCMD(maxSceneSN,timeText);
                    }

                }
                maxLiteCommand(chData);
                Toast.makeText(SceneSleepingModeActivity.this, selectedText2 + "to " + selectedText, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SceneSleepingModeActivity.this, SceneSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN",maxSceneSN);
                bundle.putString("deviceName",deviceName);
                intent.putExtras(bundle);
                //增加回去的延遲時間 避免與狀態指令重疊
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                    }
                },100);

            }
        });
    }

    private byte[] settingSceneSleepingModeCMD(byte SN[], String time) {
        byte sceneCMD[] = {0x0b, 0x06};
        byte DATATemp[] = {0x19, 0x05};
        byte lastTemp[] = {0x00, 0x00};
        byte timeCMD[] = {0x00};
        if (time.equals("min bright")) {
            timeCMD = new byte[]{(byte) 0x82};
        }
        if (time.equals("hour bright")) {
            timeCMD = new byte[]{(byte) 0x83};
        }
        if (time.equals("min sleep")) {
            timeCMD = new byte[]{(byte) 0x84};
        }
        if (time.equals("hour sleep")) {
            timeCMD = new byte[]{(byte) 0x85};
        }
        byte[] MaxScene_Temp = deviceCommand.ConcatAll(sceneCMD, SN, DATATemp,timeCMD,lastTemp);
        byte[] CRCTemp = deviceCommand.GetCRC(MaxScene_Temp);
        byte[] SendCMD = deviceCommand.ConcatAll(MaxScene_Temp, CRCTemp);
        return SendCMD;
    }
    //將處理好的指令發送出去
    private void maxLiteCommand(byte msgCRC[]) {
        Log.d(TAG, "發送的指令:" + UDP.bytesToHex(msgCRC));
        String remoteIp = "255.255.255.255";
        int port = 4000;
        // 調用UDP.java中的方法，送出資料
        exec.execute(() -> {
            try {
                MainActivity.udpServer.send(msgCRC, remoteIp, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}