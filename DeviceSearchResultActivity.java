package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;
import com.rexlite.rexlitebasicnew.ShortcutRecyclerView.ShortcutGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceSearchResultActivity extends AppCompatActivity {
    private ExpandableListView expandableListView;
    private static final String TAG = "deviceSearch";
    private static ExpandableListViewAdapter expandableListViewAdapter;
    private static List<String> listDataGroup;
    private static HashMap<String, List<Device>> listDataChild;
    private Device device;
    private Device device2;
    private static boolean test = false;
    //網路連線相關
     DeviceSearchResultActivity.MyBroadcast myBroadcast ;
    static ExecutorService exec = Executors.newCachedThreadPool();
    static LoadingDialog loadingDialog;
    static String maxSceneSN = null; // maxSceneWifi的SN
    CRC mCRC;
    boolean showDialog = true;
    Handler searchHandler = new Handler();
    CRC16_Modbus mCRC16_Modbus;
    private List<Device> deviceData = new ArrayList<Device>();
    private Toast mToastToShow;
    byte[] searchAllDevice = null;
    byte[] searchMaxSceneWifiCmd;
    static byte[]  allDeviceResult = null;
    private List<Device> devices;
    DeviceCommand deviceCommand = new DeviceCommand();
    static int addcount = 0;//避免系統重複發送廣播而重複將資料加到資料庫
    public static int failCount = 0;//判斷錯誤機制
    public int length; //byteArray的實際長度
    List<Device> max1List = new ArrayList<>();
    List<Device> max2List = new ArrayList<>();
    List<Device> max3List = new ArrayList<>();
    List<Device> maxSceneList = new ArrayList<>();
    List<Device> oneTouchList = new ArrayList<>();
    String testDevice = "070B0000000000030B0000000000541600000000000218000000000003140000000000120A00000000001C14000000000003";//當系統沒有回應的時候 測試用

    //更新相關
    final UpdateDialog updateDialog= new UpdateDialog(DeviceSearchResultActivity.this);
    private boolean finishUpdate = false;
    @Override
    public void onStart() {
        super.onStart();
        // 在此Activity啟用EventBus
        EventBus.getDefault().register(this);
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
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(DeviceSearchResultActivity.this).unregisterReceiver(myBroadcast);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(DeviceSearchResultActivity.this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        searchHandler.removeCallbacksAndMessages(null);
        LocalBroadcastManager.getInstance(DeviceSearchResultActivity.this).unregisterReceiver(myBroadcast);
        // unregisterReceiver(myBroadcast);
        Log.d(TAG, "onPause: "+111);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search_result);
        failCount = 0;
        addcount = 0;

        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button rightIcon = findViewById(R.id.right_button);
        TextView title = findViewById(R.id.toolbar_title);
        loadingDialog = new LoadingDialog(DeviceSearchResultActivity.this);
        loadingDialog.startLoadingDialog();
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        myBroadcast = new DeviceSearchResultActivity.MyBroadcast();
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceSearchResultActivity.this, MainActivity.class);
                startActivity(intent);
                Animatoo.animateSlideRight(DeviceSearchResultActivity.this);
            }
        });
        Log.d(TAG, "text: "+test);

       /* if(true) {
            Log.d(TAG, "onCreate: ");
            setSearchMaxSceneWifiFunction();
            //setSearchMaxSceneWifiFunction();
            test = true;
        }*/
        setSearchMaxSceneWifiFunction();
        //當裝置沒有回應時  過3秒後重新發送指令
        searchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (searchAllDevice == null) {
                    setSearchMaxSceneWifiFunction();
                    Log.d(TAG, "delay3: ");
                }
            }
        }, 1500);
        searchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (allDeviceResult==null) {
                    setSearchMaxSceneWifiFunction();
                    failCount = 2;
                }
            }
        }, 3000);
        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failCount = 0;
                if(allDeviceResult == null) {
                    addcount = 0;
                }

                showDialog = true;
                searchAllDevice = null;
                loadingDialog = new LoadingDialog(DeviceSearchResultActivity.this);
                loadingDialog.startLoadingDialog();
                setSearchMaxSceneWifiFunction();
                Log.d(TAG, "click發出指令: ");


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                }, 2500);
                //當裝置沒有回應時  過3秒後重新發送指令
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (searchAllDevice == null) {
                            failCount = 2;
                            setSearchMaxSceneWifiFunction();
                        }
                    }
                }, 3500);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (allDeviceResult == null) {
                            failCount = 2;
                            searchAllDeviceFunction();
                        }
                    }
                }, 6000);
            }
        });
        title.setText("Device search");
        // initializing the views
        initViews();
        // initializing the listeners
        initListeners();
        // initializing the objects
        initObjects();
        // preparing list data
        // initListData();
    }



    private void setSearchMaxSceneWifiFunction() {
       /* //指令轉換範例
        byte aa[]={(byte)0xff,0x13,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x19,(byte)0x82,0x00,0x08,(byte)0xfe};
        byte bb[]=mCRC16_Modbus.GetCRC(aa);
        byte []cc=mCRC.ConcatAll(aa,bb);*/
        byte[] msg = {0x0b, 0x13, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x19, (byte) 0x82, 0x00, 0x08, (byte) 0xfe};//(搜尋M'S)
        byte msgCRC16[] = mCRC16_Modbus.GetCRC(msg);
        byte msgCRC[] = mCRC.ConcatAll(msg, msgCRC16);
        searchMaxSceneWifiCmd = msgCRC;
        Log.d(TAG, "setSearchMaxSceneWifiFunction: " + UDP.byteArrayToHexStr(msgCRC));
        // String  msg = "要送出的訊息";
        String remoteIp = "255.255.255.255";
        int port = 4000;
        // if (msg[].length() == 0) return;
        // stringBuffer.append("發送： ").append(msg).append("\n");
        // edReceiveMessage.setText(stringBuffer);

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
                searchAllDeviceFunction();
            }
        }, 500);


    }

    private void searchAllDeviceFunction() {
        if (searchAllDevice != null) {
            byte msgCRC16[] = mCRC16_Modbus.GetCRC(searchAllDevice);
            byte msgCRC[] = mCRC.ConcatAll(searchAllDevice, msgCRC16);
            Log.d(TAG, "searchAllDeviceFunction: " + UDP.byteArrayToHexStr(msgCRC));
            // String  msg = "要送出的訊息";
            String remoteIp = "255.255.255.255";
            int port = 4000;
            // if (msg[].length() == 0) return;
            // stringBuffer.append("發送： ").append(msg).append("\n");
            // edReceiveMessage.setText(stringBuffer);

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

    /**
     * method to initialize the views
     */
    private void initViews() {
        expandableListView = findViewById(R.id.expandableListView);
    }

    /**
     * method to initialize the listeners
     */
    private void initListeners() {
        // ExpandableListView on child click listener
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
              /*  Toast.makeText(
                        getApplicationContext(),
                        listDataGroup.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataGroup.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();*/
                return false;
            }
        });
        // ExpandableListView Group expanded listener
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
               /* Toast.makeText(getApplicationContext(),
                        listDataGroup.get(groupPosition) + " " + getString(R.string.text_collapsed),
                        Toast.LENGTH_SHORT).show();*/
            }
        });
        // ExpandableListView Group collapsed listener
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                /*Toast.makeText(getApplicationContext(),
                        listDataGroup.get(groupPosition) + " " + getString(R.string.text_collapsed),
                        Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    /**
     * method to initialize the objects
     */
    private void initObjects() {
        // initializing the list of groups
        listDataGroup = new ArrayList<>();
        // initializing the list of child
        listDataChild = new HashMap<>();
        // initializing the adapter object
        expandableListViewAdapter = new ExpandableListViewAdapter(this, listDataGroup, listDataChild);
        // setting list adapter
        expandableListView.setAdapter(expandableListViewAdapter);
    }

    /*
     * Preparing the list data
     *
     * Dummy Items
     */
    private void initListData() {
        // Adding group data
       // loadingDialog.dismissDialog();
        max1List = new ArrayList<>();
        max2List = new ArrayList<>();
        max3List = new ArrayList<>();
        maxSceneList = new ArrayList<>();
        oneTouchList = new ArrayList<>();
        listDataGroup.add("MAXLiTE1");
        listDataGroup.add("MAXLiTE2");
        listDataGroup.add("MAXLiTE3");
        listDataGroup.add("MAXScene");
        // array of strings
        String[] array;
        if(addcount ==0){
        for (byte j = 0; j < allDeviceResult[0]; j++) {
            if (allDeviceResult[j * 7 + 1] == 0x0b) {
                Device device = new Device();
                byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                device.setDeviceSN(snToHex);
                device.setDeviceName("M'S_" + snToHex.substring(8));
                device.setDeviceId("0b");
                device.setDeviceIcon(R.drawable.scene);
                maxSceneList.add(device);
            }
            if (allDeviceResult[j * 7 + 1] == 0x13) {
                Device device = new Device();
                byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                device.setDeviceSN(snToHex);
                device.setDeviceName("M'A_" + snToHex.substring(8));
                device.setDeviceId("13");
                device.setDeviceIcon(R.drawable.air);
            }
            if (allDeviceResult[j * 7 + 1] == 0x14) {
                Device device = new Device();
                byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                device.setDeviceSN(snToHex);
                device.setDeviceName("M'L1_" + snToHex.substring(8));
                device.setDeviceId("14");
                device.setDeviceIcon(R.drawable.max1);
               max1List.add(device);
            }
            if (allDeviceResult[j * 7 + 1] == 0x16) {
                Device device = new Device();
                byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                device.setDeviceSN(snToHex);
                device.setDeviceName("M'L2_" + snToHex.substring(8));
                device.setDeviceId("16");
                device.setDeviceIcon(R.drawable.max2);
                max2List.add(device);

            }
            if (allDeviceResult[j * 7 + 1] == 0x18) {
                Device device = new Device();
                byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                device.setDeviceSN(snToHex);
                device.setDeviceName("M'L3_" + snToHex.substring(8));
                device.setDeviceId("18");
                device.setDeviceIcon(R.drawable.max3);
                max3List.add(device);
            }
        }
        }
        listDataChild.put(listDataGroup.get(0), max1List);
        listDataChild.put(listDataGroup.get(1), max2List);
        listDataChild.put(listDataGroup.get(2), max3List);
        listDataChild.put(listDataGroup.get(3), maxSceneList);
      /*  new Thread(() -> {
            List<Device> data = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataByDeviceId("14");
            for (int i = 0; i < data.size(); i++) {
                data.get(i).setDeviceIcon(R.drawable.max1);
            }
            max1List = data;
            List<Device> data1 = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataByDeviceId("16");
            for (int i = 0; i < data1.size(); i++) {
                data1.get(i).setDeviceIcon(R.drawable.max2);
            }
            max2List = data1;
            List<Device> data2 = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataByDeviceId("18");
            for (int i = 0; i < data2.size(); i++) {
                data2.get(i).setDeviceIcon(R.drawable.max3);
            }
            max3List = data2;
            List<Device> data3 = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataByDeviceId("0b");
            for (int i = 0; i < data3.size(); i++) {
                data3.get(i).setDeviceIcon(R.drawable.scene);
            }
            maxSceneList = data3;
            Log.d(TAG, "initListData: " + max2List);
            // Adding child data


        }).start();*/

   /*     device = new Device("ID:12345678", "MAXLite M’L-1 01", R.drawable.max1_icon);
        device2 = new Device("ID:12345677", "MAXLite M’L-1 02", R.drawable.max1_icon);
       *//* array = new String[]{"MAXLite M’L-1 01"};
        for (String item : array) {
            max1List.add(item);
        }*//*
        max1List.add(device);
        max1List.add(device2);
        // list of Max2
        List<Device> max2List = new ArrayList<>();
        *//*array =  new String[]{"MAXLite2 M’L-2 02"};
        for (String item : array) {
            max2List.add(item);
        }*//*
        device = new Device("ID:12345678", "MAXLite2 M’L-2 02", R.drawable.max2_icon);
        max2List.add(device);
        // list of Max3
        List<Device> max3List = new ArrayList<>();
        device = new Device("ID:12345678", "MAXLite3 M’L-3 03", R.drawable.max3_icon);
        *//*array =  new String[]{"MAXLite3 M’L-3 03"};
        for (String item : array) {
            max3List.add(item);
        }*//*
        max3List.add(device);
        // list of MaxScene
        List<Device> maxSceneList = new ArrayList<>();
        device = new Device("ID:12345678", "MAXScene M’S-1 02", R.drawable.scene_icon);
       *//* array =  new String[]{"MAXScene M’S-1 02"};
        for (String item : array) {
            maxSceneList.add(item);
        }*//*
        maxSceneList.add(device);*/

        // notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
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
                    if (hex.equals(UDP.byteArrayToHexStr(searchMaxSceneWifiCmd)) && searchAllDevice == null ) {
                        //setSearchMaxSceneWifiFunction();
                        Log.d(TAG, "失敗幾次: " + failCount);

                        if (failCount == 2 && showDialog && searchAllDevice == null) {
                          /*  new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //!!當MaxScene沒有回應 將存入Demo用假資料
                                    allDeviceResult = UDP.hexToByte(testDevice);
                                    //addToDataBase();

                                    showDialog = false;
                                }
                            }, 1000);*/
                            loadingDialog.dismissDialog();
                            showDialog = false;
                            Log.d(TAG, "MaxSceneWifi沒有回應");
                            new AlertDialog.Builder(DeviceSearchResultActivity.this)
                                    .setTitle("Result")
                                    .setMessage("MaxSceneWifi search fail,make sure your phone and system on the same WiFi.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }

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

                    //判斷回傳指令是所有裝置的指令

                    if (resultBytes != null && maxSceneSN != null && addcount == 0 || failCount == 2 ) {
                        if (resultBytes.length % 7 == 1 && (resultBytes.length - 1) / 7 == resultBytes[0]) {
                            Log.d(TAG, "收到裝置: "+addcount);
                            allDeviceResult = resultBytes;
                            // Log.d(TAG, "長度: "+resultBytes.length);
                            Log.d(TAG, "所有裝置: " + hex);
                            loadingDialog.dismissDialog();
                            failCount = 0;
                            if(addcount==0) {
                                initListData();
                                Log.d(TAG, "aaa: ");
                                addcount += 1;
                            }
                           // initListData();
                            addToDataBase();
                        }

                    }
                    break;

            }
        }
        private void addToDataBase() {
            int count = 0;
            for (byte j = 0; j < allDeviceResult[0]; j++) {
                if (allDeviceResult[j * 7 + 1] == 0x0b) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("M'S_" + snToHex.substring(8));
                    device.setDeviceId("0b");
                    device.setDeviceIcon(R.drawable.device_scene);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
                if (allDeviceResult[j * 7 + 1] == 0x13) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("M'A_" + snToHex.substring(8));
                    device.setDeviceId("13");
                    device.setDeviceIcon(R.drawable.device_air);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
                if (allDeviceResult[j * 7 + 1] == 0x14) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("M'L1_" + snToHex.substring(8));
                    device.setDeviceId("14");
                    device.setDeviceIcon(R.drawable.device_max1);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
                if (allDeviceResult[j * 7 + 1] == 0x16) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("M'L2_" + snToHex.substring(8));
                    device.setDeviceId("16");
                    device.setDeviceIcon(R.drawable.device_max2);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
                if (allDeviceResult[j * 7 + 1] == 0x18) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("M'L3_" + snToHex.substring(8));
                    device.setDeviceId("18");
                    device.setDeviceIcon(R.drawable.device_max3);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
                if (allDeviceResult[j * 7 + 1] == 0x0a) {
                    Device device = new Device();
                    byte getDeviceSN[] = Arrays.copyOfRange(allDeviceResult, j * 7 + 2, (j + 1) * 7 + 1);//取得SN
                    String snToHex = UDP.byteArrayToHexStr(getDeviceSN);
                    device.setDeviceSN(snToHex);
                    device.setDeviceName("One Touch");
                    device.setDeviceId("0a");
                    device.setDeviceIcon(R.drawable.device_scene);
                    deviceData.add(device);
                    //新增到資料庫
                    new Thread(() -> {
                        List<Device> deviceCheckData = new ArrayList<Device>();
                        deviceCheckData = null;//用來判斷是否已經將該裝置加入過資料庫中
                        deviceCheckData = DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(DeviceSearchResultActivity.this).getDataUao().insertData(device);

                            Shortcut shortcut = new Shortcut();
                            shortcut.setName("");
                            shortcut.setDeciveCH(" ");
                            shortcut.setHostDeviceSN(device.getDeviceSN());
                            shortcut.setHostDeviceName(device.getDeviceName());
                            shortcut.setShow(true);
                            shortcut.setType("OneTouch");
                            shortcut.setIcon(R.drawable.onetouch_icon);
                            shortcut.setDeviceType(String.format("%02X ", 0x0a));
                            ShortcutDataBase.getInstance(DeviceSearchResultActivity.this).getShortcutDataDao().insertData(shortcut);
                        }

                    }).start();
                }
            }

        }

    }
}