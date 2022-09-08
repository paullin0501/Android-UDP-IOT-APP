package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DeviceNameSettingSubtitleActivity extends AppCompatActivity {
    private static final String TAG = "subtitle";
    private int page;//記錄從哪個頁面進入
    private String deviceTye;
    byte[] deviceSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    String deviceCh;
    String selectedRoom;

    private TextView title;
    private TextView roomTitle;
    private TextView roomText;
    private  TextView subtitleTitle;
    private EditText subtitleText;

    Device device = new Device();
    List<Device>deviceList;

    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(DeviceNameSettingSubtitleActivity.this);
    private boolean finishUpdate = false;
    @Override
    public void onStart() {
        super.onStart();
        // 在此Activity啟用EventBus
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    // 註冊Subscribe
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateStatus event) {
        Log.d(TAG, "onMessageEvent: " + event.getUpgrade());
        if(event.getUpgrade().equals("0") && finishUpdate){
            loadingDialog.dismissDialog();
            finishUpdate = false;
        }
        if(event.getUpgrade().equals("1")){
            loadingDialog.startLoadingDialog();
            loadingDialog.setLoadingText("Firmware is updating...");
            finishUpdate = true;
        }
        //根據mqtt的回傳值來顯示更新的UI


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_name_setting_subtitle);
        title = findViewById(R.id.toolbar_title);
        title.setText("Edit Subtitle");
        roomTitle = findViewById(R.id.room_title);
        roomText = findViewById(R.id.room_text);
        roomTitle.setTypeface(null, Typeface.BOLD);
        subtitleTitle = findViewById(R.id.subtitle_title);
        subtitleTitle.setTypeface(null,Typeface.BOLD);
        subtitleText = findViewById(R.id.subtitle_edit);
        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        page = info.getInt("page", 0);
        deviceTye = info.getString("type");
        deviceSN = info.getByteArray("deviceSN");
        deviceName = info.getString("deviceName");
        deviceCh = info.getString("deviceCH","0");
        selectedRoom = info.getString("selectedRoom");
        roomText.setText(selectedRoom);
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button rightIcon = findViewById(R.id.right_button);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, DeviceNameSettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",deviceName);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putInt("page",page);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);

            }
        });
        rightIcon.setText("Save");

        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(()->{
                    String type=null;
                    if(device.getDeviceId().equals("14")){
                        type = String.format("%02X ", 0x14);
                    }
                    if(device.getDeviceId().equals("16")){
                        type = String.format("%02X ", 0x16);
                    }
                    if(device.getDeviceId().equals("18")){
                        type = String.format("%02X ", 0x18);
                    }
                    if(device.getDeviceId().equals("0b")){
                        type = String.format("%02X ", 0x0b);
                    }
                    if(subtitleText.getText().toString()!=null) {
                        device.setSubtitle(subtitleText.getText().toString());
                        device.setDeviceName(selectedRoom);
                        DataBase.getInstance(DeviceNameSettingSubtitleActivity.this).getDataUao().updateDeviceNameAndSubtitle(device.getId(), device.getDeviceName(),device.getSubtitle());
                        ShortcutDataBase.getInstance(DeviceNameSettingSubtitleActivity.this).getShortcutDataDao().updateDeviceHostName(device.getDeviceName(),type,device.getDeviceSN());
                    } else {
                        device.setDeviceName(selectedRoom);
                        DataBase.getInstance(DeviceNameSettingSubtitleActivity.this).getDataUao().updateDeviceName(device.getId(),device.getDeviceName());
                        ShortcutDataBase.getInstance(DeviceNameSettingSubtitleActivity.this).getShortcutDataDao().updateDeviceHostName(device.getDeviceName(),type,device.getDeviceSN());
                    }


                }).start();
                if(page==1){
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, Max1SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",selectedRoom);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
                }
                if(page==2){
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, Max2SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",selectedRoom);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
                }
                if(page==3){
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, Max3SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",selectedRoom);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
                }
                if(page==4){
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, SceneSettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",deviceSN);
                    bundle.putString("deviceName",selectedRoom);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
                }
                if (page == 5) {
                    Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, ListDeviceActivity.class);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
                }

            }
        });

        //取得該裝置物件
        new Thread(()->{
            deviceList = DataBase.getInstance(DeviceNameSettingSubtitleActivity.this).getDataUao().findDataBySNandID(deviceTye,UDP.byteArrayToHexStr(deviceSN));
            if(deviceList.size()>0) {
                device = deviceList.get(0);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(device.getSubtitle()!=null) {
                        subtitleText.setText(device.getSubtitle());
                    }
                }
            });
        }).start();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
            Intent intent = new Intent(DeviceNameSettingSubtitleActivity.this, DeviceNameSettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN",deviceSN);
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceCH",deviceCh);
            bundle.putInt("page",page);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(DeviceNameSettingSubtitleActivity.this);
    }

}