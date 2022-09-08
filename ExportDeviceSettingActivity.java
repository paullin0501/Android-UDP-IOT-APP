package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportDeviceSettingActivity extends AppCompatActivity {
    private static final String TAG = "export";
    static ExecutorService exec = Executors.newCachedThreadPool();
    ExportDeviceSettingActivity.MyBroadcast myBroadcast = new ExportDeviceSettingActivity.MyBroadcast();
    List<Device> max1List = new ArrayList<>();
    List<Device> max2List = new ArrayList<>();
    List<Device> max3List = new ArrayList<>();
    List<Device> maxSceneList = new ArrayList<>();
    List<Device> oneTouchList = new ArrayList<>();
    DeviceCommand deviceCommand = new DeviceCommand();
    static LoadingDialog loadingDialog;

    boolean showDialog = true;
    int sendTimes = 0;
    private static String importMsg = "";
    private boolean isSending = false;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ExportDeviceSettingActivity.this, MainActivity.class);
        startActivity(intent);
        Animatoo.animateSlideRight(ExportDeviceSettingActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_device_setting);
        TextView title = findViewById(R.id.toolbar_title);
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button exportBtn = findViewById(R.id.export_btn);
        Button importBtn = findViewById(R.id.import_btn);
        loadingDialog = new LoadingDialog(ExportDeviceSettingActivity.this);
        title.setText("Backup Setting");
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExportDeviceSettingActivity.this, MainActivity.class);
                startActivity(intent);
                Animatoo.animateSlideRight(ExportDeviceSettingActivity.this);
            }
        });
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
            }
        });
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // importData("{\"cmd\":\"set device\",\"cnt\":1,\"device\":{\"dev_1\":{\"channelConfig\":{\"ch_1\":{\"devType\":\"MAX-1CT\",\"isShortcut\":0,\"name\":\"C\"},\"ch_2\":{\"devType\":\"MAX-2CT\",\"isShortcut\":0,\"name\":\"CH2\"}},\"devClass\":1,\"devType\":\"16\",\"name\":\"M'L2_0002\",\"sn\":\"000000000002\",\"subtitle\":\"\"}},\"host\":\"REXLiTE V4 Tools\"}");
                if (importMsg.length() > 0) {
                    importData(importMsg);
                }
            }
        });
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);

    }

    private void exportData() {
        showDialog = true;
        isSending = true;
        new Thread(() -> {
            runOnUiThread(new Runnable() {
                public void run() {
                    // UI code goes here
                    loadingDialog.startLoadingDialog("Data Sending...");
                }
            });
            List<Device> data = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataByDeviceId("14");
            max1List = data;
            List<Device> data1 = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataByDeviceId("16");
            max2List = data1;
            List<Device> data2 = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataByDeviceId("18");
            max3List = data2;
            List<Device> data3 = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataByDeviceId("0b");
            maxSceneList = data3;
            List<Device> data4 = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataByDeviceId("0a");
            oneTouchList = data4;

            //最外層Json
            ExportData exportData = new ExportData();
            int count = 0;
            int deviceCnt = 1;
            count = count + max1List.size();
            count = count + max2List.size();
            count = count + max3List.size();
            exportData.setCnt(1);
            //Device層
            List<ExportDevice> exportDeviceList = new ArrayList<>();
            //都包成JSON Object物件需使用HashMap


            //放入裝置
            for (Device device : max1List) {

                Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();

                ExportDevice exportDevice = new ExportDevice();
                exportDevice.setName(device.getDeviceName());
                if(device.getSubtitle() == null){
                    exportDevice.setSubtitle("");
                } else {
                    exportDevice.setSubtitle(device.getSubtitle());
                }

                exportDevice.setDevClass(2);
                exportDevice.setType(device.getDeviceId());
                exportDevice.setSn(device.getDeviceSN());
                Log.d(TAG, "1: " + exportDevice.getName());
                //捷徑部分
                List<Shortcut> shortcutList = new ArrayList<>();
                shortcutList = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandType(device.getDeviceSN(), String.format("%02X ", 0x14));
                if (shortcutList.size() > 0) {
                Shortcut shortcut = shortcutList.get(0);
                Map<String, ChannelConfig> channelConfigList = new HashMap<String, ChannelConfig>();

                    ChannelConfig channelConfig = new ChannelConfig();
                    channelConfig.setName(shortcut.getName());
                    channelConfig.setDevType(shortcut.getType());
                    int booInt = shortcut.isShow() ? 1 : 0;
                    channelConfig.setShortcut(booInt);
                    channelConfigList.put("ch_" + 1, channelConfig);
                    exportDevice.setChannelConfig(channelConfigList);
                } else {
                    Map<String, ChannelConfig> channelConfigList = new HashMap<String, ChannelConfig>();
                    Shortcut shortcut = new Shortcut();
                    shortcut.setName("CH1");
                    shortcut.setType("");
                    shortcut.setShow(false);
                    ChannelConfig channelConfig = new ChannelConfig();
                    channelConfig.setName(shortcut.getName());
                    channelConfig.setDevType(shortcut.getType());
                    int booInt = shortcut.isShow() ? 1 : 0;
                    channelConfig.setShortcut(booInt);
                    channelConfigList.put("ch_" + 1, channelConfig);
                    exportDevice.setChannelConfig(channelConfigList);
                }

               /* ExportDeviceOutside exportDeviceOutside = new ExportDeviceOutside();
                exportDeviceOutside.setExportDevice(exportDevice);
                exportDeviceOutside.setOutsideName("dev_"+deviceCnt);
                exportDeviceOutsideList.add(exportDeviceOutside);*/
                // deviceMap.put("dev_"+deviceCnt,exportDevice);
                deviceMap.put("dev_1", exportDevice);
                deviceCnt++;
            /*    if(deviceCnt==2){
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                    sendCommand(jsonStr);
                }*/
                try {
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                    sendCommand(jsonStr);
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // exportDeviceList.add(exportDevice);
            }
            for (Device device : max2List) {

                Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();
                ExportDevice exportDevice = new ExportDevice();
                exportDevice.setName(device.getDeviceName());
                if(device.getSubtitle() == null){
                    exportDevice.setSubtitle("");
                } else {
                    exportDevice.setSubtitle(device.getSubtitle());
                }
                exportDevice.setDevClass(2);
                exportDevice.setType(device.getDeviceId());
                exportDevice.setSn(device.getDeviceSN());
                Log.d(TAG, "2: " + exportDevice.getName());
                //捷徑部分
                List<Shortcut> shortcutList = new ArrayList<>();
                shortcutList = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandType(device.getDeviceSN(), String.format("%02X ", 0x16));
                Map<String, ChannelConfig> channelConfigList = new HashMap<String, ChannelConfig>();
                if (shortcutList.size() > 0) {
                    for (int i = 0; i < 2; i++) {
                        ChannelConfig channelConfig = new ChannelConfig();
                        Shortcut shortcut = shortcutList.get(i);
                        channelConfig.setName(shortcut.getName());

                        channelConfig.setDevType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        channelConfig.setShortcut(booInt);
                        channelConfigList.put("ch_" + (i + 1), channelConfig);
                    }
                    //未加入shortcut時
                } else {
                    for (int i = 0; i < 2; i++) {
                        ChannelConfig channelConfig = new ChannelConfig();
                        Shortcut shortcut = new Shortcut();
                        shortcut.setName("CH"+(i+1));
                        shortcut.setType("");
                        shortcut.setShow(false);
                        channelConfig.setName(shortcut.getName());

                        channelConfig.setDevType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        channelConfig.setShortcut(booInt);
                        channelConfigList.put("ch_" + (i + 1), channelConfig);
                    }
                }
                exportDevice.setChannelConfig(channelConfigList);
               /* ExportDeviceOutside exportDeviceOutside = new ExportDeviceOutside();
                exportDeviceOutside.setExportDevice(exportDevice);
                exportDeviceOutside.setOutsideName("dev_"+deviceCnt);
                exportDeviceOutsideList.add(exportDeviceOutside);*/
                //  deviceMap.put("dev_"+deviceCnt,exportDevice);
                deviceMap.put("dev_1", exportDevice);
                deviceCnt++;
                try {
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                    sendCommand(jsonStr);
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Device device : max3List) {

                Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();
                ExportDevice exportDevice = new ExportDevice();
                exportDevice.setName(device.getDeviceName());
                if(device.getSubtitle() == null){
                    exportDevice.setSubtitle("");
                } else {
                    exportDevice.setSubtitle(device.getSubtitle());
                }
                exportDevice.setDevClass(2);
                exportDevice.setType(device.getDeviceId());
                exportDevice.setSn(device.getDeviceSN());
                //捷徑部分
                List<Shortcut> shortcutList = new ArrayList<>();
                shortcutList = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandType(device.getDeviceSN(), String.format("%02X ", 0x18));
                Map<String, ChannelConfig> channelConfigList = new HashMap<String, ChannelConfig>();
                if (shortcutList.size() > 0) {
                    for (int i = 0; i < 3; i++) {
                        ChannelConfig channelConfig = new ChannelConfig();
                        Shortcut shortcut = shortcutList.get(i);
                        channelConfig.setName(shortcut.getName());
                        channelConfig.setDevType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        channelConfig.setShortcut(booInt);
                        channelConfigList.put("ch_" + (i + 1), channelConfig);
                    }
                }  else {
                    for (int i = 0; i < 3; i++) {
                        ChannelConfig channelConfig = new ChannelConfig();
                        Shortcut shortcut = new Shortcut();
                        shortcut.setName("CH" + (i + 1));
                        shortcut.setType("");
                        shortcut.setShow(false);
                        channelConfig.setName(shortcut.getName());

                        channelConfig.setDevType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        channelConfig.setShortcut(booInt);
                        channelConfigList.put("ch_" + (i + 1), channelConfig);
                    }
                }
                exportDevice.setChannelConfig(channelConfigList);
             /*   ExportDeviceOutside exportDeviceOutside = new ExportDeviceOutside();
                exportDeviceOutside.setExportDevice(exportDevice);
                exportDeviceOutside.setOutsideName("dev_"+deviceCnt);
                exportDeviceOutsideList.add(exportDeviceOutside);*/
                //deviceMap.put("dev_"+deviceCnt,exportDevice);
                deviceMap.put("dev_1", exportDevice);
                deviceCnt++;
                try {
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                   // jsonStr = toUtf8(jsonStr);
                    sendCommand(jsonStr);
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (Device device : maxSceneList) {
                Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();
                ExportDevice exportDevice = new ExportDevice();
                exportDevice.setName(device.getDeviceName());
                if(device.getSubtitle() == null){
                    exportDevice.setSubtitle("");
                } else {
                    exportDevice.setSubtitle(device.getSubtitle());
                }
                exportDevice.setDevClass(1);
                exportDevice.setType(device.getDeviceId());
                exportDevice.setSn(device.getDeviceSN());
                Log.d(TAG, "4: " + exportDevice.getName());
                //捷徑部分
                List<Shortcut> shortcutList = new ArrayList<>();
                shortcutList = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandType(device.getDeviceSN(), String.format("%02X ", 0x0b));
                Map<String, SceneConfig> channelConfigList = new HashMap<String, SceneConfig>();
                if (shortcutList.size() > 0) {
                    for (int i = 0; i < 6; i++) {
                        SceneConfig sceneConfig = new SceneConfig();
                        Shortcut shortcut = shortcutList.get(i);
                        sceneConfig.setName(shortcut.getName());
                        sceneConfig.setShortcutType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        sceneConfig.setShortcut(booInt);
                        channelConfigList.put("scene_" + (i + 1), sceneConfig);
                    }
                }
                else {
                    for (int i = 0; i < 6; i++) {
                        SceneConfig channelConfig = new SceneConfig();
                        Shortcut shortcut = new Shortcut();
                        shortcut.setName("Scene " + (i + 1));
                        shortcut.setType("");
                        shortcut.setShow(false);
                        channelConfig.setName(shortcut.getName());
                        channelConfig.setShortcutType(shortcut.getType());
                        int booInt = shortcut.isShow() ? 1 : 0;
                        channelConfig.setShortcut(booInt);
                        channelConfigList.put("scene_" + (i + 1), channelConfig);
                    }
                }
                exportDevice.setSceneConfig(channelConfigList);
                deviceMap.put("dev_1", exportDevice);
                deviceCnt++;
                try {
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                    sendCommand(jsonStr);
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Device device : oneTouchList) {

                Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();

                ExportDevice exportDevice = new ExportDevice();
                exportDevice.setName(device.getDeviceName());
                if(device.getSubtitle() == null){
                    exportDevice.setSubtitle("");
                } else {
                    exportDevice.setSubtitle(device.getSubtitle());
                }

                exportDevice.setDevClass(1);
                exportDevice.setType(device.getDeviceId());
                exportDevice.setSn(device.getDeviceSN());
                deviceMap.put("dev_1", exportDevice);
                deviceCnt++;

                try {
                    exportData.setDevice(deviceMap);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String jsonStr;
                    jsonStr = gson.toJson(exportData);
                    sendCommand(jsonStr);
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            showDialog = false;
            isSending = false;
            runOnUiThread(new Runnable() {
                public void run() {
                    // UI code goes here
                    loadingDialog.dismissDialog();
                }
            });
        }).start();

    }

    private void importData(String data) {
        new Thread(() -> {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            ImportData importData = new ImportData();
          /*  String newData = "";
            try {
                newData  = URLDecoder.decode(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                byte[] utf8 = data.getBytes("ISO_8859_1");
              newData  =  new String(utf8,"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            importData = gson.fromJson(newData, ImportData.class);*/
            importData = gson.fromJson(data, ImportData.class);
            Map<String, ExportDevice> deviceMap = new HashMap<String, ExportDevice>();
            // Log.d(TAG, "importData: "+importData.getCmd());
            //取得裝置
            deviceMap = importData.getDevice();
            //Log.d(TAG, "importData: "+ deviceMap.get("dev_1").getChannelConfig());
            if (deviceMap.get("dev_1").getType().equals("14")) {
                checkData(deviceMap, 1);
            }
            if (deviceMap.get("dev_1").getType().equals("16")) {
                checkData(deviceMap, 2);
            }
            if (deviceMap.get("dev_1").getType().equals("18")) {
                checkData(deviceMap, 3);
            }
            if (deviceMap.get("dev_1").getType().equals("0b")) {
                checkSceneData(deviceMap);
            }
            if (deviceMap.get("dev_1").getType().equals("0a")) {
                checkOneTouchData(deviceMap);
            }
        }).start();

    }
    private void checkOneTouchData(Map<String, ExportDevice> deviceMap){
        Map<String, ChannelConfig> channelConfigMap = new HashMap<String, ChannelConfig>();
        //與資料庫的裝置比對
        List<Device> deviceList = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(), deviceMap.get("dev_1").getSn());
        if (deviceList.size() == 0) {
            Log.d(TAG, "找不到裝置 準備新增... ");
            //設定裝置
            Device device = new Device();
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            device.setDeviceIcon(R.drawable.onetouch_icon);
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().insertData(device);
            //設定shortcut

            //拆出裝置的各個shortcut並存入資料庫
                Shortcut shortcut = new Shortcut();
                shortcut.setName("");
                shortcut.setDeciveCH(" ");
                shortcut.setHostDeviceSN(device.getDeviceSN());
            shortcut.setHostDeviceName(device.getDeviceName());
                shortcut.setShow(true);
                shortcut.setType("OneTouch");
                shortcut.setIcon(R.drawable.onetouch_icon);
                shortcut.setDeviceType(String.format("%02X ", 0x0a));
                ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);


        } else {
            Log.d(TAG, "找到裝置 準備更新... ");
            //設定裝置
            Device device = new Device();
            List<Device> deviceList1 = new ArrayList<Device>();
            deviceList1 =  DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(),deviceMap.get("dev_1").getSn());
            if(deviceList1.size() >0){
                int id = deviceList1.get(0).getId();
                device.setId(id);
            }
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            device.setDeviceIcon(R.drawable.onetouch_icon);
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().updateData(device);
            Shortcut shortcut = new Shortcut();
            shortcut.setName("OneTouch");
            shortcut.setDeciveCH("OneTouch");
            shortcut.setHostDeviceSN(device.getDeviceSN());
            shortcut.setHostDeviceName(device.getDeviceName());
            shortcut.setShow(false);
            shortcut.setType("OneTouch");
            shortcut.setIcon(R.drawable.onetouch_icon);
            shortcut.setDeviceType(String.format("%02X ", 0x0a));
            List<Shortcut> list = new ArrayList<Shortcut>();
            list = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandTypeandCH(device.getDeviceSN(), shortcut.getDeviceType(), shortcut.getDeciveCH());
            if(list.size()==0){
                ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
            }
        }
    }
    private void checkData(Map<String, ExportDevice> deviceMap, int type) {
        int img = 0;
        int typeNum = 0x00;
        switch (type) {
            case 1:
                img = R.drawable.device_max1;
                typeNum = 0x14;
                break;
            case 2:
                img = R.drawable.device_max2;
                typeNum = 0x16;
                break;
            case 3:
                img = R.drawable.device_max3;
                typeNum = 0x18;
                break;



        }
        Map<String, ChannelConfig> channelConfigMap = new HashMap<String, ChannelConfig>();
        //與資料庫的裝置比對
        List<Device> deviceList = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(), deviceMap.get("dev_1").getSn());
        if (deviceList.size() == 0) {
            Log.d(TAG, "找不到裝置 準備新增... ");
            //設定裝置
            Device device = new Device();
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            device.setDeviceIcon(img);
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().insertData(device);
            //設定shortcut
           /* for (int i = 1; i < 3; i++) {
                Shortcut shortcut = new Shortcut();
                shortcut.setName("CH" + i);
                shortcut.setDeciveCH("CH" + i);
                shortcut.setHostDeviceSN(device.getDeviceSN());
                shortcut.setDeviceType(String.format("%02X ", 0x16));

               // shortcut.setIcon(R);
                shortcut.setHostDeviceName(device.getDeviceName());
                ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
            }*/
            channelConfigMap = deviceMap.get("dev_1").getChannelConfig();
            //拆出裝置的各個shortcut並存入資料庫
            for (int i = 1; i <= channelConfigMap.size(); i++) {
                Log.d(TAG, ": " + channelConfigMap.get("ch_" + i).getName());
                Shortcut shortcut = new Shortcut();
                shortcut.setName(channelConfigMap.get("ch_" + i).getName());
                shortcut.setDeciveCH("CH" + i);
                shortcut.setHostDeviceSN(device.getDeviceSN());
                shortcut.setHostDeviceName(device.getDeviceName());
                boolean b = false;
                if (channelConfigMap.get("ch_" + i).isShortcut() == 1) {
                    b = true;
                } else {
                    b = false;
                }
                shortcut.setShow(b);
                int icon = getDeviceIconNum(channelConfigMap.get("ch_" + i).getDevType());
                shortcut.setType(channelConfigMap.get("ch_" + i).getDevType());
                shortcut.setIcon(icon);
                shortcut.setDeviceType(String.format("%02X ", typeNum));
                ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
            }

        } else {
            Log.d(TAG, "找到裝置 準備更新... ");
            //設定裝置
            Device device = new Device();
            List<Device> deviceList1 = new ArrayList<Device>();
            deviceList1 =  DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(),deviceMap.get("dev_1").getSn());
            if(deviceList1.size() >0){
                int id = deviceList1.get(0).getId();
                device.setId(id);
            }
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            device.setDeviceIcon(img);
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().updateData(device);

            channelConfigMap = deviceMap.get("dev_1").getChannelConfig();
            //拆出裝置的各個shortcut並存入資料庫
            for (int i = 1; i <= channelConfigMap.size(); i++) {
                Log.d(TAG, ": " + channelConfigMap.get("ch_" + i).getName());
                Shortcut shortcut = new Shortcut();
                shortcut.setName(channelConfigMap.get("ch_" + i).getName());
                shortcut.setDeciveCH("CH" + i);
                shortcut.setHostDeviceSN(device.getDeviceSN());
                shortcut.setHostDeviceName(device.getDeviceName());
                boolean b = false;
                if (channelConfigMap.get("ch_" + i).isShortcut() == 1) {
                    b = true;
                } else {
                    b = false;
                }
                shortcut.setShow(b);
                int icon = getDeviceIconNum(channelConfigMap.get("ch_" + i).getDevType());
                shortcut.setType(channelConfigMap.get("ch_" + i).getDevType());
                shortcut.setIcon(icon);
                shortcut.setDeviceType(String.format("%02X ", typeNum));
                List<Shortcut> list = new ArrayList<Shortcut>();
                list = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandTypeandCH(device.getDeviceSN(), shortcut.getDeviceType(), shortcut.getDeciveCH());
                Log.d(TAG, "checkSceneData: "+device.getDeviceSN()+" "+shortcut.getDeviceType()+" "+shortcut.getDeciveCH()+" "+list.size());
                if (list.size() > 0) {
                    int id = list.get(0).getId();
                    shortcut.setId(id);
                    ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().updateData(shortcut);

                } else {
                    ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
                }
                //  ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().updateData(id,shortcut);
            }
        }
    }

    private void checkSceneData(Map<String, ExportDevice> deviceMap) {
        Map<String, SceneConfig> sceneConfigMap = new HashMap<String, SceneConfig>();
        //與資料庫的裝置比對
        List<Device> deviceList = DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(), deviceMap.get("dev_1").getSn());
        if (deviceList.size() == 0) {
            Log.d(TAG, "找不到裝置 準備新增... ");
            //設定裝置
            Device device = new Device();
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            device.setDeviceIcon(R.drawable.device_scene);
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().insertData(device);
            //設定shortcut
            sceneConfigMap = deviceMap.get("dev_1").getSceneConfig();
            //拆出裝置的各個shortcut並存入資料庫
            for (int i = 1; i <= sceneConfigMap.size(); i++) {
                Shortcut shortcut = new Shortcut();
                shortcut.setName(sceneConfigMap.get("scene_" + i).getName());
                shortcut.setDeciveCH("Scene" + i);
                shortcut.setHostDeviceSN(device.getDeviceSN());
                shortcut.setHostDeviceName(device.getDeviceName());
                boolean b = false;
                if (sceneConfigMap.get("scene_" + i).isShortcut() == 1) {
                    b = true;
                } else {
                    b = false;
                }
                shortcut.setShow(b);
                int icon = getSceneIconNum(sceneConfigMap.get("scene_" + i).getShortcutType());
                shortcut.setType(sceneConfigMap.get("scene_" + i).getShortcutType());
                shortcut.setIcon(icon);
                shortcut.setDeviceType(String.format("%02X ", 0x0b));
                ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
            }

        } else {
            Log.d(TAG, "找到裝置 準備更新... ");
            //設定裝置
            Device device = new Device();
            List<Device> deviceList1 = new ArrayList<Device>();
            deviceList1 =  DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().findDataBySNandID(deviceMap.get("dev_1").getType(),deviceMap.get("dev_1").getSn());
            if(deviceList1.size() >0){
                int id = deviceList1.get(0).getId();
                device.setId(id);
            }
            device.setDeviceName(deviceMap.get("dev_1").getName());
            device.setDeviceIcon(R.drawable.device_scene);
            device.setDeviceId(deviceMap.get("dev_1").getType());
            device.setDeviceSN(deviceMap.get("dev_1").getSn());
            if (deviceMap.get("dev_1").getSubtitle() != null) {
                device.setSubtitle(deviceMap.get("dev_1").getSubtitle());
            }
            DataBase.getInstance(ExportDeviceSettingActivity.this).getDataUao().updateData(device);

            sceneConfigMap = deviceMap.get("dev_1").getSceneConfig();
            //拆出裝置的各個shortcut並存入資料庫
            for (int i = 1; i <= sceneConfigMap.size(); i++) {
                //Log.d(TAG, ": " + sceneConfigMap.get("ch_" + i).getName());
                Shortcut shortcut = new Shortcut();
                shortcut.setName(sceneConfigMap.get("scene_" + i).getName());
                shortcut.setDeciveCH("Scene " + i);
                shortcut.setHostDeviceSN(device.getDeviceSN());
                shortcut.setHostDeviceName(device.getDeviceName());
                //將0 1轉換成布林
                boolean b = false;
                if (sceneConfigMap.get("scene_" + i).isShortcut() == 1) {
                    b = true;
                } else {
                    b = false;
                }
                shortcut.setShow(b);
                int icon = getSceneIconNum(sceneConfigMap.get("scene_" + i).getShortcutType());
                shortcut.setType(sceneConfigMap.get("scene_" + i).getShortcutType());
                shortcut.setIcon(icon);
                shortcut.setDeviceType(String.format("%02X ", 0x0b));
                List<Shortcut> list = new ArrayList<Shortcut>();
                list = ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().findDataByhostSNandTypeandCH(device.getDeviceSN(), shortcut.getDeviceType(), shortcut.getDeciveCH());
                Log.d(TAG, "checkSceneData: "+device.getDeviceSN()+" "+shortcut.getDeviceType()+" "+shortcut.getDeciveCH()+" "+list.size());
                if (list.size() > 0) {
                    int id = list.get(0).getId();
                    Log.d(TAG, "更新: ");
                    shortcut.setId(id);
                    ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().updateData(shortcut);

                }
                else {
                    ShortcutDataBase.getInstance(ExportDeviceSettingActivity.this).getShortcutDataDao().insertData(shortcut);
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
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

                    //收到PC匯出指令
                    //Log.d(TAG, "bytes: "+resultBytes);
                    if (msg.contains("query device list")) {
                        exportData();
                    }
                    if(!isSending) {
                        if (msg.contains("Android")) {
                            importData(msg);
                            importMsg = msg;
                        }
                        if (msg.contains("iOS")) {
                            importData(msg);
                            importMsg = msg;
                        }
                        if (msg.contains("set device")) {
                            importData(msg);
                            importMsg = msg;
                        }
                        if (msg.contains("find device")) {
                            setFindCommand(msg);
                        }
                    }
                    //importData(msg);
                    String hex = UDP.byteArrayToHexStr(resultBytes);
                    Log.d(TAG, "onReceive: " + msg);

            }
        }
    }
    private void setFindCommand(String data){
        byte[] sendCMD;
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        ImportData importData = new ImportData();
        importData = gson.fromJson(data, ImportData.class);
        FindDevice findDevice = importData.getFindDevice();
        byte[] devType = {};
        switch(findDevice.getDevType()){
            case "0b":
                devType = new byte[] {0x0b};
                break;
            case "0a":
                devType = new byte[] {0x0a};
                break;
            case "14":
                devType = new byte[] {0x14};
                break;
            case "16":
                devType = new byte[] {0x16};
                break;
            case "18":
                devType = new byte[] {0x18};
                break;

        }
        sendCMD = deviceCommand.settingSearchDeviceCMD(devType,true,UDP.hexToByte(findDevice.getSn()));
        maxLiteCommand(sendCMD);
    }


    //發送指令
    private void maxLiteCommand(byte msgCRC[]) {
        Log.d(TAG, "發送指令: " + UDP.byteArrayToHexStr(msgCRC));
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

    private int getSceneIconNum(String scene) {
        int icon = 0;
        switch (scene) {
            case "0":
                icon = R.drawable.scene_icon1_on;
                break;
            case "1":
                icon = R.drawable.scene_icon2_on;
                break;
            case "2":
                icon = R.drawable.scene_icon3_on;
                break;
            case "3":
                icon = R.drawable.scene_icon4_on;
                break;
            case "4":
                icon = R.drawable.scene_icon5_on;
                break;
            case "5":
                icon = R.drawable.scene_icon6_on;
                break;
            case "6":
                icon = R.drawable.scene_icon7_on;
                break;
            case "7":
                icon = R.drawable.scene_icon8_on;
                break;
            case "8":
                icon = R.drawable.scene_icon9_on;
                break;
        }
        return icon;
    }

    private int getDeviceIconNum(String device) {
        int icon = 0;
        switch (device) {
            case "MAX-1CT":
                icon = R.drawable.brightness_icon;
                break;
            case "MAX-2CT":
                icon = R.drawable.temperature_icon;
                break;
            case "MAX-Relay":
                icon = R.drawable.relay_icon;
                break;
            case "MAX-Drapes":
                icon = R.drawable.curtain_icon;
                break;
            case "MAX-Drapes(angle)":
                icon = R.drawable.curtain_degree_icon;
                break;
            case "MAX-Drapes(stepless)":
                icon = R.drawable.curtain_degree_icon;
                break;
        }
        return icon;
    }

    //發送指令
    private void sendCommand(String data) {
        sendTimes++;
        Log.d(TAG, "發送: " + sendTimes + data);
        String remoteIp = "255.255.255.255";
        int port = 4000;
        // 調用UDP.java中的方法，送出資料
        exec.execute(() -> {
            try {
                MainActivity.udpServer.send(data, remoteIp, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public static String toUtf8(String str) {
        String newStr = "";
        try {
            newStr = new String(str.getBytes("UTF-8"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return newStr;
    }
}