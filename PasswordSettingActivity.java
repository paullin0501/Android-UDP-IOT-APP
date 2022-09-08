package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.zxing.common.StringUtils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordSettingActivity extends AppCompatActivity {
    private static final String TAG = "passwordsetting";
    PasswordSettingActivity.MyBroadcast myBroadcast = new PasswordSettingActivity.MyBroadcast();
    String systemPassword;
    String systemMAC;
    String systemVersion;
    DeviceCommand deviceCommand = new DeviceCommand();
    static ExecutorService exec = Executors.newCachedThreadPool();
    static View loadingAni;
    static LoadingDialog loadingDialog;
    byte[] checkFunction;//確認有無取得密碼的變數
    public static int failCount = 0;
    boolean showDialog = true;

    private static EditText currentPassword;
    private static EditText newPassword ;
    private static EditText confirmPassword;

    //更新相關
    final UpdateDialog updateDialog = new UpdateDialog(PasswordSettingActivity.this);
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
            updateDialog.dismissDialog();
            finishUpdate = false;
        }
        if(event.getUpgrade().equals("1")){
            updateDialog.startLoadingDialog();
            updateDialog.setLoadingText("Firmware is updating...");
            finishUpdate = true;
        }
        //根據mqtt的回傳值來顯示更新的UI


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(myBroadcast);
        Animatoo.animateSlideRight(PasswordSettingActivity.this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_setting);
        failCount = 0;
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
         currentPassword = (EditText) findViewById(R.id.current_passwd);
         newPassword = (EditText) findViewById(R.id.new_passwd);
         confirmPassword = (EditText) findViewById(R.id.confirm_passwd);
        Button saveButton = findViewById(R.id.save_button);
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        systemMAC = info.getString("systemMAC","");
        systemVersion = info.getString("systemVersion","");
        title.setText("Reset System Password");
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(myBroadcast);
                Intent intent = new Intent(PasswordSettingActivity.this, UserSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("systemMAC",systemMAC);
                bundle.putString("systemVersion",systemVersion);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideRight(PasswordSettingActivity.this);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + currentPassword.getText().toString().equals(systemPassword));
                if (!currentPassword.getText().toString().equals(systemPassword)) {
                    StyleableToast toast = StyleableToast.makeText(PasswordSettingActivity.this, "Current password is not correct.", Toast.LENGTH_LONG, R.style.exampleToast);
                    toast.show();
                } else if (newPassword.getText().toString().length() < 6) {
                    StyleableToast toast = StyleableToast.makeText(PasswordSettingActivity.this, "New password must be at least 6 characters long.", Toast.LENGTH_LONG, R.style.exampleToast);
                    toast.show();
                } else if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    StyleableToast toast = StyleableToast.makeText(PasswordSettingActivity.this, "Confirm password and new password are not match.", Toast.LENGTH_LONG, R.style.exampleToast);
                    toast.show();

                } else {
                    loadingDialog = new LoadingDialog(PasswordSettingActivity.this);
                    loadingDialog.startLoadingDialog("Setting password...");
                    String password;
                    byte[] end = {(byte)0x0d,(byte)0x0a};
                    //須帶入密碼長度
                    String passwordLength = String.format("%02X", newPassword.getText().toString().length()+2);

                    password = passwordLength + asciiToHex(newPassword.getText().toString());
                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte(password), "resetPassword");
                    Log.d(TAG, "密碼設定指令: " + UDP.byteArrayToHexStr(command));
                    Log.d(TAG, "數量: "+passwordLength);
                    maxSceneCommand(command);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            byte[] command = deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword");
                            maxSceneCommand(command);

                        }
                    },300);

                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(systemPassword.equals(newPassword.getText().toString())){
                                loadingDialog.dismissDialog();
                                StyleableToast toast = StyleableToast.makeText(PasswordSettingActivity.this, "Password setting success.", Toast.LENGTH_LONG, R.style.exampleToast_success);
                                toast.show();
                                unregisterReceiver(myBroadcast);
                                Intent intent = new Intent(PasswordSettingActivity.this, UserSettingActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("systemMAC",systemMAC);
                                bundle.putString("systemVersion",systemVersion);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                Animatoo.animateSlideRight(PasswordSettingActivity.this);
                            }
                            else {
                                StyleableToast toast = StyleableToast.makeText(PasswordSettingActivity.this, "Password setting failed. Please try again", Toast.LENGTH_LONG, R.style.exampleToast);
                                toast.show();
                            }
                        }
                    },2000);


                }

            }
        });
        //限制輸入的字元以及最大長度
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; ++i) {
                    if (!Pattern.compile("[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890]*").matcher(String.valueOf(source.charAt(i))).matches()) {
                        return "";
                    }
                }
                return null;
            }
        };
        currentPassword.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(8)});
        newPassword.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(8)});
        confirmPassword.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(8)});
        //取得動畫View
        loadingAni = findViewById(R.id.spin_kit);
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        byte[] command = deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword");
        Log.d(TAG, "密碼詢問指令: " + UDP.byteArrayToHexStr(command));
        maxSceneCommand(command);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(systemPassword==null) {
                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword");
                    maxSceneCommand(command);
                }
            }
        },500);

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(systemPassword==null) {
                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword");
                    failCount=2;
                    maxSceneCommand(command);
                }
            }
        },2000);
    }

    private void maxSceneCommand(byte msgCRC[]) {
        String remoteIp = "255.255.255.255";
        Log.d(TAG, "maxSceneCommand: " + UDP.byteArrayToHexStr(msgCRC));
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

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }

    //回傳接收資料處理
    private class MyBroadcast extends BroadcastReceiver {
        private List<String> deviceResultData = new ArrayList<String>();

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            assert mAction != null;
            switch (mAction) {
                /**接收來自UDP回傳之訊息*/
                case UDP.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(UDP.RECEIVE_STRING);
                    byte[] bytes = intent.getByteArrayExtra(UDP.RECEIVE_BYTES);//接收到裝置回傳的bytes陣列
                    Bundle bundle = intent.getExtras();
                    int length = bundle.getInt(UDP.RECEIVE_DATALENGTH);//資料實際的長度 這樣才可以拿出多餘的0
                    //將多餘的0濾除
                    byte[] resultBytes = new byte[length];
                    for (int i = 0; i < length; i++) {
                        resultBytes[i] = bytes[i];
                    }
                    //Log.d(TAG, "bytes: "+resultBytes);
                    String hex = UDP.byteArrayToHexStr(resultBytes);
                    Log.d(TAG, "onReceive: " + hex);

                    if (hexToAscii(hex).contains("MAC")) {

                        systemMAC = hexToAscii(hex).substring(3, 20);
                        //正規表示式取得密碼 regex
                        Matcher matcher;
                        matcher = Pattern.compile("PWD([A-Za-z0-9]{6,8})").matcher(hexToAscii(hex));
                        if (matcher.find()) {
                            //matcher.find();
                            systemPassword = matcher.group();
                            systemPassword = systemPassword.substring(3);
                            Log.d(TAG, "password: " + systemPassword);
                        }

                        Log.d(TAG, "MAC: " + systemMAC);
                        loadingAni.setVisibility(View.GONE);

                    } else if(failCount ==2 && showDialog){
                        loadingAni.setVisibility(View.GONE);
                        currentPassword.setEnabled(false);
                        newPassword.setEnabled(false);
                        confirmPassword.setEnabled(false);
                        showDialog = false;
                        new AlertDialog.Builder(PasswordSettingActivity.this)
                                .setTitle("Connect Failed")
                                .setMessage("can't check system password!")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    // Log.d(TAG, "長度: " + hex.length());
                  /*  if (hex.equals(UDP.byteArrayToHexStr(searchMaxSceneWifiCmd)) && searchAllDevice == null ) {
                        //setSearchMaxSceneWifiFunction();
                        Log.d(TAG, "失敗幾次: " + failCount);

                        if (failCount == 2 && showDialog && searchAllDevice == null) {
                          *//*  new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //!!當MaxScene沒有回應 將存入Demo用假資料
                                    allDeviceResult = UDP.hexToByte(testDevice);
                                    //addToDataBase();

                                    showDialog = false;
                                }
                            }, 1000);*//*
                            loadingDialog.dismissDialog();
                            showDialog = false;
                            Log.d(TAG, "MaxSceneWifi沒有回應");
                            new AlertDialog.Builder(DeviceSearchResultActivity.this)
                                    .setTitle("搜尋結果")
                                    .setMessage("MaxSceneWifi搜尋失敗，請確認手機是否與裝置在同一個WiFi")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }*/

                    break;

            }
        }
    }
}