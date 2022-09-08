package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.github.javiersantos.appupdater.AppUpdater;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserSettingActivity extends AppCompatActivity {
    private static final String TAG = "usersetting";
    //cmd相關變數
    byte[] searchAllDevice = null;
    byte[] searchMaxSceneWifiCmd;
    String lockStatus; //判斷面板是否鎖定
    CRC16_Modbus mCRC16_Modbus;
    CRC mCRC;
    UserSettingActivity.MyBroadcast myBroadcast =  new UserSettingActivity.MyBroadcast();
    DeviceCommand deviceCommand = new DeviceCommand();
    static LoadingDialog2 loadingDialog;
    static ExecutorService exec = Executors.newCachedThreadPool();
    static String maxSceneSN = null; // maxSceneWifi的SN
    //ListView 要顯示的內容
    public String[][] data = {{" ", " "}};
    public String[][] data1 = {{" ", " "}};
    public String[][] data2 = {{" ", " "}};
    public String[][] data3 = {{" ", " "}};
    SimpleAdapter adapter1;
    SimpleAdapter adapter;
    List<Map<String, Object>> items1;
    List<Map<String, Object>> items;
    public String firmwareVersion;
    public String firmwareNum = "4.00";
    TextView settingText;
    ImageView settingImg;

    private String systemMAC;
    private String systemVersion;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(myBroadcast);
        Animatoo.animateSlideRight(UserSettingActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        systemMAC = info.getString("systemMAC","");
        systemVersion = info.getString("systemVersion","");

        //選單設定
        settingText = findViewById(R.id.setting_text);
        settingImg = findViewById(R.id.setting_img);
        loadingDialog = new LoadingDialog2(UserSettingActivity.this);
        loadingDialog.startLoadingDialog();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(lockStatus==null) {
                    loadingDialog.dismissDialog();
                }
            }
        }, 3000);
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Setting");
        firmwareVersion = "Version " + firmwareNum;
        data = new String[][]{
                {"Version", firmwareNum},
                {"Update on Google Play", ""},
                {"Reset Application", ""},
        };
        //scene
        data1 = new String[][]{
                {"Disable scenes setting", ""}
        };
        data2 = new String[][]{
                {"Firmware update", ""}
        };
        data3 = new String[][]{
                {"Reset system password", ""}
        };
        //將資料轉換成<key,value>的型態
        items = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("level", data[i][0]);
            item.put("name", data[i][1]);
            items.add(item);
        }
        //scene
        items1 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data1.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("level", data1[i][0]);
            item.put("name", data1[i][1]);
            items1.add(item);
        }

        List<Map<String, Object>> items2 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data2.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("level", data2[i][0]);
            item.put("name", data2[i][1]);
            items2.add(item);
        }
        List<Map<String, Object>> items3 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data3.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("level", data3[i][0]);
            item.put("name", data3[i][1]);
            items3.add(item);
        }

        //帶入對應資料
         adapter = new SimpleAdapter(
                this,
                items,
                R.layout.app_style_listview,
                new String[]{"level", "name"},
                new int[]{R.id.item_text, R.id.end_text}
        );
         adapter1 = new SimpleAdapter(
                this,
                items1,
                R.layout.app_style_listview,
                new String[]{"level", "name"},
                new int[]{R.id.item_text, R.id.end_text}
        );
        SimpleAdapter adapter2 = new SimpleAdapter(
                this,
                items2,
                R.layout.scene_shortcut_item,
                new String[]{"level", "name"},
                new int[]{R.id.item_text}
        );
        SimpleAdapter adapter3 = new SimpleAdapter(
                this,
                items3,
                R.layout.scene_shortcut_item,
                new String[]{"level", "name"},
                new int[]{R.id.item_text}
        );

        ListView listview = (ListView) findViewById(R.id.app_list);
        ListView sceneListView = (ListView) findViewById(R.id.scene_list);
        ListView firmwareListView = (ListView) findViewById(R.id.firmware_list);
        ListView passwordListView = (ListView) findViewById(R.id.password_list);
        listview.setAdapter(adapter);
        sceneListView.setAdapter(adapter1);
        firmwareListView.setAdapter(adapter2);
        passwordListView.setAdapter(adapter3);
        listview.setOnItemClickListener(onClickListView);
        sceneListView.setOnItemClickListener(onClickListView1);
        firmwareListView.setOnItemClickListener(onClickListView2);
        passwordListView.setOnItemClickListener(onClickListView3);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if(myBroadcast!=null) {
                    unregisterReceiver(myBroadcast);
                }*/
                Intent intent = new Intent(UserSettingActivity.this, MainActivity.class);
                startActivity(intent);
                Animatoo.animateSlideRight(UserSettingActivity.this);
            }
        });

        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        setSearchMaxSceneWifiFunction();


    }

    //1
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 1) {
                // Toast.makeText(UserSettingActivity.this,"update", Toast.LENGTH_SHORT).show();
                AppUpdater appUpdater = new AppUpdater(UserSettingActivity.this);
                appUpdater.setButtonDoNotShowAgain(null);
                appUpdater.setButtonDismiss("Cancel");
                appUpdater.showAppUpdated(true);
                appUpdater.setTitleOnUpdateNotAvailable("Update not available");
                appUpdater.setContentOnUpdateNotAvailable("No update available.Your App is the latest version!");
                appUpdater.start();
            }
            if (position == 2) {
                // Toast.makeText(UserSettingActivity.this,"Reset", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(UserSettingActivity.this)
                        .setMessage("Are you sure you want to reset the app and remove all data and settings?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new Thread(() -> {
                                    DataBase.getInstance(UserSettingActivity.this).getDataUao().nukeTable();
                                    ShortcutDataBase.getInstance(UserSettingActivity.this).getShortcutDataDao().nukeTable();
                                }).start();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        }

    };
    //2 lock setting
    private AdapterView.OnItemClickListener onClickListView1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                String msg;
                if(lockStatus.equals("FF")){
                    msg = "Disable Scenes setting on panels?";
                    new AlertDialog.Builder(UserSettingActivity.this)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "lock");
                                    Log.d(TAG, "指令: " + UDP.byteArrayToHexStr(command));
                                    maxSceneCommand(command);

                                    settingText.setText("Scenes setting on panels is locked.");
                                    settingImg.setImageResource(R.drawable.lock);
                                    data1 = new String[][]{
                                            {"Enable scenes setting", ""}
                                    };
                                    for (int i = 0; i < data1.length; i++) {
                                        Map<String, Object> item = new HashMap<String, Object>();
                                        item.put("level", data1[i][0]);
                                        item.put("name", data1[i][1]);
                                        items1.set(0,item);
                                    }
                                    adapter1.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                if(lockStatus.equals("00")){
                    msg = "Enable Scenes setting on panels?";
                    new AlertDialog.Builder(UserSettingActivity.this)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "unlock");
                                    Log.d(TAG, "指令: " + UDP.byteArrayToHexStr(command));
                                    maxSceneCommand(command);
                                    settingText.setText("Now you can set Scenes on panels.");
                                    settingImg.setImageResource(R.drawable.unlock);
                                    data1 = new String[][]{
                                            {"Disable scenes setting", ""}
                                    };
                                    for (int i = 0; i < data1.length; i++) {
                                        Map<String, Object> item = new HashMap<String, Object>();
                                        item.put("level", data1[i][0]);
                                        item.put("name", data1[i][1]);
                                        items1.set(0,item);
                                    }
                                    adapter1.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                if(lockStatus==null){
                    msg = "Disable Scenes setting on panels?";
                    new AlertDialog.Builder(UserSettingActivity.this)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "lock");
                                    Log.d(TAG, "指令: " + UDP.byteArrayToHexStr(command));
                                    maxSceneCommand(command);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //了解裝置的狀態
                                                byte[] command = deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "status");
                                                Log.d(TAG, "狀態指令: " + UDP.byteArrayToHexStr(command));
                                                maxSceneCommand(command);
                                        }
                                    }, 1000);
                                    settingText.setText("Now you can set Scenes on panels.");
                                    settingImg.setImageResource(R.drawable.unlock);
                                    data1 = new String[][]{
                                            {"Disable scenes setting", ""}
                                    };
                                    for (int i = 0; i < data1.length; i++) {
                                        Map<String, Object> item = new HashMap<String, Object>();
                                        item.put("level", data1[i][0]);
                                        item.put("name", data1[i][1]);
                                        items1.set(0,item);
                                    }
                                    adapter1.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
               // Toast.makeText(UserSettingActivity.this, "Enable", Toast.LENGTH_SHORT).show();

            }
        }

    };
    //3 firmware
    private AdapterView.OnItemClickListener onClickListView2 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                unregisterReceiver(myBroadcast);
               // MainActivity.m_mqttManager.disconnect();
               // MainActivity.m_mqttManager = null;
                Intent intent = new Intent(UserSettingActivity.this,FirmwareUpdateActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("systemMAC",systemMAC);
                bundle.putString("systemVersion",systemVersion);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(UserSettingActivity.this);
            }
        }

    };
    //4 password
    private AdapterView.OnItemClickListener onClickListView3 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                //Toast.makeText(UserSettingActivity.this, "password", Toast.LENGTH_SHORT).show();
                unregisterReceiver(myBroadcast);
                Bundle bundle = new Bundle();
                bundle.putString("systemMAC",systemMAC);
                bundle.putString("systemVersion",systemVersion);
                startActivity(new Intent(UserSettingActivity.this, PasswordSettingActivity.class).putExtras(bundle));
                Animatoo.animateSlideLeft(UserSettingActivity.this);
            }

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
    }

    private void setSearchMaxSceneWifiFunction() {
        byte[] msg = {0x0b, 0x13, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x19, (byte) 0x82, 0x00, 0x08, (byte) 0xfe};//(搜尋M'S)
        byte msgCRC16[] = mCRC16_Modbus.GetCRC(msg);
        byte msgCRC[] = mCRC.ConcatAll(msg, msgCRC16);
        searchMaxSceneWifiCmd = msgCRC;
        Log.d(TAG, "setSearchMaxSceneWifiFunction: " + UDP.byteArrayToHexStr(msgCRC));
        // String  msg = "要送出的訊息";
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //了解裝置的狀態
                if (searchAllDevice != null) {
                    byte[] command = deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "status");
                    Log.d(TAG, "狀態指令: " + UDP.byteArrayToHexStr(command));
                    maxSceneCommand(command);
                }
            }
        }, 500);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    maxSceneCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkVersion"));

            }
        }, 2200);
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
                    /**確保收到的回傳值正確再進行SN擷取*/
                    if (hex.contains("1B0B")) {
                       /* new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        }, 300);*/
                        maxSceneSN = hex.substring(4, 16); //取得AP模式 M'S的SN
                        Log.d(TAG, "MSWiFiSN: " + maxSceneSN);
                        byte[] searchAllDeviceDefault = {0x0b, 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, 0x19, 0x30, 0x02, 0x08, 0x07};
                        String searchAllDeviceCustom = UDP.byteArrayToHexStr(searchAllDeviceDefault);
                        searchAllDeviceCustom = searchAllDeviceCustom.replace("000000000007", maxSceneSN); //將Commend中的SN換成MSWiFiSN
                        // Log.d(TAG, "SearchCMD: "+searchAllDeviceCustom);
                        searchAllDevice = UDP.hexToByte(searchAllDeviceCustom);
                    }
                    if(hex.length()==50 && maxSceneSN != null){
                        loadingDialog.dismissDialog();
                        lockStatus = hex.substring(4,6);
                        Log.d(TAG, "是否鎖定(00鎖定):  "+lockStatus);
                        if(lockStatus.equals("00")){
                            settingText.setText("Scenes setting on panels is locked.");
                            settingImg.setImageResource(R.drawable.lock);
                            data1 = new String[][]{
                                    {"Enable scenes setting", ""}
                            };
                            for (int i = 0; i < data1.length; i++) {
                                Map<String, Object> item = new HashMap<String, Object>();
                                item.put("level", data1[i][0]);
                                item.put("name", data1[i][1]);
                                items1.set(0,item);
                            }
                            adapter1.notifyDataSetChanged();

                        }

                        if(lockStatus.equals("FF")){
                            settingText.setText("Now you can set Scenes on panels.");
                            settingImg.setImageResource(R.drawable.unlock);
                            data1 = new String[][]{
                                    {"Disable scenes setting", ""}
                            };
                            for (int i = 0; i < data1.length; i++) {
                                Map<String, Object> item = new HashMap<String, Object>();
                                item.put("level", data1[i][0]);
                                item.put("name", data1[i][1]);
                                items1.set(0,item);
                            }
                            adapter1.notifyDataSetChanged();
                        }
                    }
                    //取得版本資訊
                    if (hexToAscii(hex).contains("MSW")) {

                        firmwareNum = hexToAscii(hex).substring(3, 4)+"."+hexToAscii(hex).substring(4);
                        Log.d(TAG, "Version: " + firmwareNum);
                        data = new String[][]{
                                {"Version", firmwareNum},
                                {"Update on Google Play", ""},
                                {"Reset Application", ""},
                        };


                            Map<String, Object> item = new HashMap<String, Object>();
                            item.put("level", data[0][0]);
                            item.put("name", data[0][1]);
                            items.set(0,item);

                        adapter.notifyDataSetChanged();

                    }
                    break;

            }
        }
    }
    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}
