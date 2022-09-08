package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.facebook.stetho.Stetho;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.zxing.common.StringUtils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.SceneStatus;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;
import com.rexlite.rexlitebasicnew.ShortcutRecyclerView.ShortcutGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rexlite.rexlitebasicnew.LocalIP.getLocalIP;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_LOGIN = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static boolean logon = false;
    final UpdateDialog loadingDialog = new UpdateDialog(MainActivity.this);
    //網路連線相關
    MyBroadcast myBroadcast = new MyBroadcast();
    ExecutorService exec = Executors.newCachedThreadPool();
    public static UDP udpServer;
    CRC mCRC;
    DeviceCommand deviceCommand = new DeviceCommand();
    CRC16_Modbus mCRC16_Modbus;
    static String maxSceneSN = null; // maxSceneWifi的SN
    private DrawerLayout drawer;
    private List<Device> deviceData = new ArrayList<Device>();
    byte[] searchAllDevice = null;
    byte[] searchMaxSceneWifiCmd;
    byte[] allDeviceResult = null;
    int addcount = 0;//避免系統重複發送廣播而重複將資料加到資料庫
    int failCount = 0;//判斷錯誤機制
    public int length; //byteArray的實際長度
    String testDevice = "050b0000000000030a00000000001c160000000000020b00000000005418000000000003";//當系統沒有回應的時候 測試用
    Handler handler1 = new Handler();
    Runnable runnable1;
    boolean resume = false; //判斷是否從別的頁面回來
    static boolean oneTouchStatus = false;
    static boolean hasOneTouch = false;
    static boolean controlling = false;
    Runnable runnable; //計時器
    Handler handler = new Handler(); //計時器
    List<Shortcut> shortcuts;
    List<ShortcutGroup> groups = new ArrayList<>(); //捷徑的集合
    static List<SceneStatus> sceneDeviceList = new ArrayList<>();//用於顯示狀態用 情境裝置的集合 *******
    //recyclerView相關變數
    RecyclerView mRecyclerView;
    ImageView helper;
    int spanCount = 3; // 3 columns
    int spacing = 45; // 45px
    boolean includeEdge = true;
    RecyclerAdapter mainRecyclerAdapter;

    //更新相關
    private static AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 100;
    public static MqttManager m_mqttManager;
    private UpdateStatus updateStatus;
    private boolean connectMqtt = false;
    private boolean finishUpdate = false;
    private Handler mHandler;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    //登入相關
    private static String systemVersion="4.0";
    private static String systemPassword;
    private static String userPassword;
    private static String systemMAC;
    private static  boolean showPassword = true;
    private static boolean systemEnter = false;




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
    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        //一般menu可能無法顯示icon時使用的
        // getMenuInflater().inflate(R.menu.menu_main,menu);
       /* try {  //顯示menu的Icons
            Class<?> clazz = Class . forName ("androidx.appcompat.view.menu.MenuBuilder");
            Method m = clazz . getDeclaredMethod ("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
      /*  int positionOfMenuItem = 0;
        MenuItem item = menu.getItem(positionOfMenuItem);
        SpannableString s = new SpannableString("My red MenuItem");
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 6);
        item.setTitle(s);*/

        return super.onCreateOptionsMenu(menu);
    }

    /*   @Override
       protected void onDestroy() {
           loadingDialog.dismissDialog();
           super.onDestroy();
       }*/
    @Override
    public void onBackPressed() {
     /*new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme)
             .setTitle("Exit")
             .setMessage("確定要關閉程式")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener()
             {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     finish();
                 }

             })
             .setPositiveButton("cancel", null)
            .show();*/
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                        // finishAndRemoveTask();

                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = findViewById(R.id.helper_image);
        failCount = 0;
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);//設置資料庫監視
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //關閉toolbar的標題文字
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);

        //修改hamburger icon 的大小
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if (toolbar.getChildAt(i) instanceof ImageButton) {
                toolbar.getChildAt(i).setScaleX(1.6f);
                toolbar.getChildAt(i).setScaleY(1.6f);
            }
        }
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        /*delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    DataBase.getInstance(MainActivity.this).getDataUao().nukeTable();
                }).start();
            }
        });*/


        //RecyclerView畫面
      /*  final LayoutInflater layoutInflater = LayoutInflater.from(this);

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

            }
        };*/
//app更新相關
        mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(result, AppUpdateType.FLEXIBLE, MainActivity.this
                                , RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /*//MQTT連接
        MqttManager.upgrade_topic = "max-system/V4/" + systemMAC + "/upgrade";
        m_mqttManager = MqttManager.getInstance(MainActivity.this);
        m_mqttManager.connect("android", mHandler);*/


      /*  Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                m_mqttManager.publish("123456");
            }
        }, 700);*/

        //取得捷徑資料
        new Thread(() -> {
            shortcuts = ShortcutDataBase.getInstance(MainActivity.this).getShortcutDataDao().findisShow(true);
            List<Shortcut> deviceShortcut = new ArrayList<>();
            List<Shortcut> sceneShortcut = new ArrayList<>();
            String groupname = "Devices";
            String groupname1 = "Scenes";
            String max1 = String.format("%02X ", 0x14);
            String max2 = String.format("%02X ", 0x16);
            String max3 = String.format("%02X ", 0x18);
            String oneTouch = String.format("%02X ", 0x0a);
            Drawable icon = getResources().getDrawable(R.drawable.shortcut_device, null);
            Drawable icon1 = getResources().getDrawable(R.drawable.shortcut_scene, null);

            //將shortcut分類
            if (shortcuts.size() != 0) {
                for (int i = 0; i < shortcuts.size(); i++) {

                    if (shortcuts.get(i).getDeviceType().equals(max1) || shortcuts.get(i).getDeviceType().equals(max2) || shortcuts.get(i).getDeviceType().equals(max3)|| shortcuts.get(i).getDeviceType().equals(oneTouch)) {
                        deviceShortcut.add(shortcuts.get(i));
                    } else {
                        sceneShortcut.add(shortcuts.get(i));
                    }
                }
            }
            if (sceneShortcut.size() != 0) {
                groups.add(new ShortcutGroup(groupname1, icon1, sceneShortcut));
            }
            if (deviceShortcut.size() != 0) {
                groups.add(new ShortcutGroup(groupname, icon, deviceShortcut));
            }

        }).start();

        //設定計時設定是否處在使用者操作的狀態
        runnable = new Runnable() {
            @Override
            public void run() {
                controlling = false;
                handler.postDelayed(this, 2000);
            }
        };
        runnable.run(); //執行計時器
        mRecyclerView = findViewById(R.id.shortcut_recyclerView);
        mainRecyclerAdapter = new RecyclerAdapter(groups);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "shortcut: " + groups.size());
                //  mRecyclerView.setHasFixedSize(true);
                // mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this,3));
                // mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
                mRecyclerView.setAdapter(mainRecyclerAdapter);
                if (shortcuts.size() > 0) {
                    helper = findViewById(R.id.helper_image);
                    helper.setVisibility(View.GONE);
                }
                if (shortcuts.size() == 0) {
                    helper = findViewById(R.id.helper_image);
                    helper.setVisibility(View.VISIBLE);
                }
            }
        }, 100);


        //判斷是否登入
        logon = getSharedPreferences("Data", Context.MODE_PRIVATE)
                .getBoolean("log", false);
        if (!logon) {
            Intent intent = new Intent(this, GuideActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }
        if (logon) {
            // loadingDialog.startLoadingDialog();
            resume = getIntent().getBooleanExtra("resume", false);
            userPassword =  getSharedPreferences("Data", Context.MODE_PRIVATE)
                    .getString("userPassword", "");

            if (resume) {
                // loadingDialog.dismissDialog();
                Log.d(TAG, "onCreate: " + true);
                setSearchMaxSceneWifiFunction();
            }
            if (!resume) {
                Log.d(TAG, "onCreate: " + false);
                //當裝置沒有回應時  過3秒後重新發送指令
               /* new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (searchAllDevice == null) {
                            setSearchMaxSceneWifiFunction();
                        }
                    }
                }, 3000);*/

            }
            connectUDP();
        }
    }

    //更新相關
    private InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(@NonNull InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdate();
            }
        }
    };

    //更新相關
    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "New app is ready!"
                , Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.show();
    }

    //判斷是否更新成功
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //取消更新
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        /*if (requestCode == REQUEST_LOGIN) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }*/
    }

    @Override
    protected void onStop() {
        if (mAppUpdateManager != null)
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

/* @Override
    protected void onDestroy() {
        super.onDestroy();
     Bundle bundle = getArguments();
    }*/

/* @Override
    protected void onDestroy() {
        super.onDestroy();
        udpServer.changeServerStatus(false);
    }*/
/*    @Override
    protected void onPause() {
        super.onPause();
        *//*if(udpServer!=null) {
            udpServer.changeServerStatus(false);

        }*//*
        MainActivity.this.unregisterReceiver(myBroadcast);
    }*/


    private void connectUDP() {
    /* //清除所有儲存過的資訊
     stringBuffer.delete(0,stringBuffer.length());*/
        Log.d(TAG, "connectUDP: " + "dddd");
        udpServer = new UDP(getLocalIP(this), this);
        //開啟UDP伺服器監聽
        int port = 4000;
        udpServer.setPort(port);
        udpServer.changeServerStatus(true);
        exec.execute(udpServer);
        setSearchMaxSceneWifiFunction();

        /*//關閉UDP伺服器監聽
        udpServer.changeServerStatus(false);*/


        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
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
                udpServer.send(msgCRC, remoteIp, port);


            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //詢問資訊相關
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( systemPassword == null ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword"));
                }
            }
        }, 100);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( systemPassword == null ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword"));
                }
            }
        }, 1100);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( systemPassword == null ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword"));
                }
            }
        }, 2200);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!systemVersion.equals("4.0") ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkVersion"));
                }
            }
        }, 3200);
       /* Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!systemVersion.equals("4.0") && systemPassword == null ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword"));
                }
            }
        }, 100);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!systemVersion.equals("4.0") ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkVersion"));
                }
            }
        }, 300);
             handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (systemVersion.equals("4.0")) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkVersion"));
                }
            }
        }, 500);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (systemVersion.equals("4.0")) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkVersion"));
                }
            }
        }, 700);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!systemVersion.equals("4.0") && systemPassword == null ) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "checkPassword"));
                }
            }
        }, 1000);*/




        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (maxSceneSN != null) {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "shortcut"));
                    Log.d(TAG, "1: " + maxSceneSN);
                } else {
                    sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "shortcut"));
                    Log.d(TAG, "2: " + maxSceneSN);
                }
            }
        }, 600);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sceneDeviceList == null) {
                    if (maxSceneSN != null) {
                        sendCommand(deviceCommand.MaxScene(UDP.hexToByte(maxSceneSN), "shortcut"));
                        Log.d(TAG, "1: " + maxSceneSN);
                    } else {
                        sendCommand(deviceCommand.MaxScene(UDP.hexToByte("FFFFFFFFFFFF"), "shortcut"));
                        Log.d(TAG, "2: " + maxSceneSN);
                    }
                }
            }
        }, 2000);




    }

    //發送指令
    private void sendCommand(byte msgCRC[]) {
        Log.d(TAG, "發送指令: " + UDP.byteArrayToHexStr(msgCRC));
        String remoteIp = "255.255.255.255";
        int port = 4000;
        // 調用UDP.java中的方法，送出資料
        exec.execute(() -> {
            try {
                udpServer.send(msgCRC, remoteIp, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
                    udpServer.send(msgCRC, remoteIp, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //進入控制畫面
    public void goDevice(View view) {
        if(systemEnter) {
            Intent deviceIntent = new Intent(this, Device3Activity.class);
//            m_mqttManager.publish("12345");
            startActivity(deviceIntent);
            Animatoo.animateSlideLeft(this);
        } else {
            Toast.makeText(this,"Can't connect to system please check your WiFi or can't get system WiFi.",Toast.LENGTH_LONG).show();
        }

    }
   /* @Override
    public void onBackPressed() {
       // super.onBackPressed();
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else  {
            super.onBackPressed();
        }
    }*/


    //功能選單實作
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //DeviceScan功能
        switch (item.getItemId()) {
            /*case R.id.action_user_profile:
                Toast.makeText(this,"user_profile",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onNavigationItemSelected: ");
                break;*/
            case R.id.action_device_search:
                // Toast.makeText(this,"device_search",Toast.LENGTH_SHORT).show();
                if(systemEnter) {
                    startActivity(new Intent(MainActivity.this, DeviceSearchResultActivity.class));
                    Animatoo.animateSlideLeft(this);
                } else {
                    Toast.makeText(this,"Can't connect to system please check your WiFi.",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_device_rescan:
                startActivity(new Intent(MainActivity.this, GuideActivity.class));
                Animatoo.animateSlideLeft(this);
                // Toast.makeText(this,"device_rescan",Toast.LENGTH_SHORT).show();
                /*if(systemEnter) {
                    startActivity(new Intent(MainActivity.this, GuideActivity.class));
                    Animatoo.animateSlideLeft(this);
                }else {
                    Toast.makeText(this,"Can't connect to system please check your WiFi.",Toast.LENGTH_LONG).show();
                }*/
                break;
            case R.id.action_list_device_id:
                if(systemEnter) {
                    startActivity(new Intent(MainActivity.this, ListDeviceActivity.class));
                    Animatoo.animateSlideLeft(this);
                }else {
                    Toast.makeText(this,"Can't connect to system please check your WiFi.",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_setting:
                if(systemEnter) {
                    Intent intent = new Intent(MainActivity.this,UserSettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("systemMAC",systemMAC);
                    bundle.putString("systemVersion",systemVersion);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(this);
                }else {
                    Toast.makeText(this,"Can't connect to system please check your WiFi.",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_export_setting:
                Intent intent = new Intent(MainActivity.this,ExportDeviceSettingActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(this);
                break;
            case R.id.ac_setting:
                Intent intent1 = new Intent(MainActivity.this,AirControlActivity.class);
                startActivity(intent1);
                Animatoo.animateSlideLeft(this);
                break;
          /*  case R.id.action_reset_maxscene:
                Toast.makeText(this,"reset_maxscene",Toast.LENGTH_SHORT).show();
                break;
           */
           /* case R.id.action_help:
                Toast.makeText(this,"help",Toast.LENGTH_SHORT).show();
                break;*/
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //標題部分的adapter
    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        List<ShortcutGroup> sectionList;

        public RecyclerAdapter(List<ShortcutGroup> sectionList) {
            this.sectionList = sectionList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.shortcut_title, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MainActivity.RecyclerAdapter.ViewHolder holder, int position) {

            ShortcutGroup section = sectionList.get(position);
            String sectionName = section.getTitle();
            Drawable sectionIcon = section.getIcon();
            List<Shortcut> items = section.getShortcutList();

            holder.titleTextView.setText(sectionName);
            holder.icon.setImageDrawable(sectionIcon);
            ItemRecyclerAdapter childRecyclerAdapter = new ItemRecyclerAdapter(items, sceneDeviceList);
            /*holder.childRecyclerView.setHasFixedSize(true);
            holder.childRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this,3));
            holder.childRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, 30, includeEdge));*/
            holder.childRecyclerView.setAdapter(childRecyclerAdapter);


        }

        @Override
        public int getItemCount() {
            return sectionList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView titleTextView;
            RecyclerView childRecyclerView;
            ImageView icon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                titleTextView = itemView.findViewById(R.id.title_text);
                icon = itemView.findViewById(R.id.title_icon);
                childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
            }
        }
    }

    private void itemClicked(Shortcut shortcut) {
        String max1 = String.format("%02X ", 0x14);
        String max2 = String.format("%02X ", 0x16);
        String max3 = String.format("%02X ", 0x18);
        String oneTouch = String.format("%02X ", 0x0a);
        if (shortcut.getDeviceType().equals(max1)) {
            Intent intent = new Intent(MainActivity.this, Max1SettingActivity.class);
            byte[] deviceSN = UDP.hexToByte(shortcut.getHostDeviceSN());
            Bundle sendInfo = new Bundle();
            sendInfo.putByteArray("deviceSN", deviceSN);
            sendInfo.putString("deviceName", shortcut.getHostDeviceName());
            sendInfo.putString("deviceCH", shortcut.getDeciveCH().substring(2));
            intent.putExtras(sendInfo);
            startActivity(intent);
        }
        if (shortcut.getDeviceType().equals(max2)) {
            Intent intent = new Intent(MainActivity.this, Max2SettingActivity.class);
            byte[] deviceSN = UDP.hexToByte(shortcut.getHostDeviceSN());
            Bundle sendInfo = new Bundle();
            sendInfo.putByteArray("deviceSN", deviceSN);
            sendInfo.putString("deviceName", shortcut.getHostDeviceName());
            sendInfo.putString("deviceCH", shortcut.getDeciveCH().substring(2));
            intent.putExtras(sendInfo);
            startActivity(intent);
        }
        if (shortcut.getDeviceType().equals(max3)) {
            Intent intent = new Intent(MainActivity.this, Max3SettingActivity.class);
            byte[] deviceSN = UDP.hexToByte(shortcut.getHostDeviceSN());
            Bundle sendInfo = new Bundle();
            sendInfo.putByteArray("deviceSN", deviceSN);
            sendInfo.putString("deviceName", shortcut.getHostDeviceName());
            sendInfo.putString("deviceCH", shortcut.getDeciveCH().substring(2));
            intent.putExtras(sendInfo);
            startActivity(intent);
        }
        if (shortcut.getDeviceType().equals(oneTouch)) {
           if(!oneTouchStatus){
               oneTouchStatus = true;
               controlling = true;
               byte[] deviceSN = UDP.hexToByte(shortcut.getHostDeviceSN());
               byte[] cmd = deviceCommand.oneTouchCommand(deviceSN,true);
               sendCommand(cmd);
           }
           else {
                oneTouchStatus = false;
                controlling = true;
                byte[] deviceSN = UDP.hexToByte(shortcut.getHostDeviceSN());
                byte[] cmd = deviceCommand.oneTouchCommand(deviceSN,false);
                sendCommand(cmd);
            }
        }
       /* if(shortcut.getDeviceType().equals(scene)){
            Log.d(TAG, "DDDDDDDD: ");
            if(!bottomBtn.isSelected()){
                bottomBtn.setSelected(true);
            }
            if(bottomBtn.isSelected()){
                bottomBtn.setSelected(false);
            }
        }*/


    }

    //底部資料的adapter
    class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder> {

        List<Shortcut> items;
        List<SceneStatus> statusItems;
        int clickPosition = -1;
        int itemNum = 0;
        List<Boolean> btnStatus = new ArrayList<>();

        public ItemRecyclerAdapter(List<Shortcut> items, List<SceneStatus> statusItems) {
            this.items = items;
            this.statusItems = statusItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.shortcut_item, parent, false);
            itemNum++;
            btnStatus.add(false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MainActivity.ItemRecyclerAdapter.ViewHolder holder, int position) {
            String status = "";
            String max1 = String.format("%02X ", 0x14);
            String max2 = String.format("%02X ", 0x16);
            String max3 = String.format("%02X ", 0x18);
            String scene = String.format("%02X ", 0x0b);
            String oneTouch = String.format("%02X ", 0x0a);
            holder.itemNameText.setText(items.get(position).getHostDeviceName());
            holder.itemChText.setText(items.get(position).getName());
            //如果屬於device就不會有開關按鍵
            if (items.get(position).getDeviceType().equals(max1) || items.get(position).getDeviceType().equals(max2) || items.get(position).getDeviceType().equals(max3)
                    || items.get(position).getDeviceType().equals(oneTouch)) {
                holder.bottomBtn.setVisibility(View.GONE);
            }
            //  sceneDeviceList.indexOf(items.get(position).getHostDeviceSN());
            //  Log.d(TAG, "size: "+statusItems.size());
            if (sceneDeviceList == null) {
                return;
            }
            for (int i = 0; i < statusItems.size(); i++) {
                if (statusItems.get(i).getDeviceSN().equals(items.get(position).getHostDeviceSN())) {
                    // int m =  statusItems.indexOf(items.get(position).getHostDeviceSN());
                    //  SceneStatus sceneDevice =  statusItems.get(m);
                    status = statusItems.get(i).getStatus();
                }
            }
            //
            //  Log.d(TAG, "status: "+status);
            // Log.d(TAG, "status: "+items.get(position).getName());
            //  Log.d(TAG, "status: "+status);
            sceneStatus(holder, position, status);
            items.get(position).getDeviceType();
            final Shortcut shortcut = items.get(position);
            // holder.bottomBtn.setSelected(false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(systemEnter) {
                        itemClicked(shortcut);
                        //  Log.d(TAG, "status: "+holder.bottomBtn.isSelected());
                        //   Log.d(TAG, "pos: "+position);
                        sceneShortcutClick(shortcut, scene, holder, position);
                        //將按鈕設定成單選
                   /* for(int i = 0 ; i <itemNum ; i++) {
                        if(position != i) {
                            notifyItemChanged(i);
                        }
                    }*/
                    } else {
                        Toast.makeText(MainActivity.this,"Can't connect to system please check your WiFi.",Toast.LENGTH_LONG).show();
                    }
                }
            });
            holder.bottomBtn.setVisibility(View.GONE);
            holder.itemIconView.setImageResource(items.get(position).getIcon());
        }

        //判斷哪時候需要亮燈
        private void sceneStatus(@NonNull ViewHolder holder, int position, String status) {
            /*Log.d(TAG, "1: "+items.get(position).getDeciveCH());
            Log.d(TAG, "2: "+status);
            Log.d(TAG, "3: "+items.get(position).getDeciveCH().equals(status));*/
            int orange = ContextCompat.getColor(MainActivity.this, R.color.orange);
            int gray = ContextCompat.getColor(MainActivity.this, R.color.dark_gray);
            if (items.get(position).getDeciveCH().equals(status) && !status.equals("power")) {
                holder.bottomBtn.setSelected(true);
                holder.itemChText.setTextColor(orange);
                holder.itemNameText.setTextColor(orange);
                holder.itemIconView.setColorFilter(orange);
            } else {
                String scene = String.format("%02X ", 0x0b);
                if (items.get(position).getDeviceType().equals(scene)) {
                    holder.bottomBtn.setSelected(false);
                    holder.itemChText.setTextColor(gray);
                    holder.itemNameText.setTextColor(gray);
                    holder.itemIconView.setColorFilter(gray);
                }
            }
            if(hasOneTouch && !oneTouchStatus){
                String scene = String.format("%02X ", 0x0b);
                if (items.get(position).getDeviceType().equals(scene)) {
                    holder.bottomBtn.setSelected(false);
                    holder.itemChText.setTextColor(gray);
                    holder.itemNameText.setTextColor(gray);
                    holder.itemIconView.setColorFilter(gray);
                }
            }
            //判斷One Touch狀態
            if (items.get(position).getHostDeviceName().equals("One Touch")) {
                if(oneTouchStatus) {
                    holder.bottomBtn.setSelected(true);
                    holder.itemChText.setTextColor(orange);
                    holder.itemNameText.setTextColor(orange);
                    holder.itemIconView.setColorFilter(orange);
                }
                if(!oneTouchStatus){
                    holder.bottomBtn.setSelected(false);
                    holder.itemChText.setTextColor(gray);
                    holder.itemNameText.setTextColor(gray);
                    holder.itemIconView.setColorFilter(gray);
                }
            }
        }

        //當個別點擊時按鈕會開關
        private void sceneShortcutClick(Shortcut shortcut, String scene, @NonNull ViewHolder holder, int position) {
            controlling = true;
            byte[] command;
            String sceneCh;
            if (shortcut.getDeviceType().equals(scene)) {
                if (holder.bottomBtn.isSelected()) {
                    for (int i = 0; i < statusItems.size(); i++) {
                        if (statusItems.get(i).getDeviceSN().equals(items.get(position).getHostDeviceSN())) {
                            statusItems.get(i).setStatus("power");
                        }
                    }
                    //  holder.bottomBtn.setSelected(false);
                    command = deviceCommand.MaxScene(UDP.hexToByte(items.get(position).getHostDeviceSN()), "power");
                    sendCommand(command);
                } else if (!holder.bottomBtn.isSelected()) {

                    boolean isAdd = false;
                    for (int i = 0; i < statusItems.size(); i++) {
                        //   holder.bottomBtn.setSelected(true);
                        if (statusItems.get(i).getDeviceSN().equals(items.get(position).getHostDeviceSN())) {
                            statusItems.get(i).setStatus(items.get(position).getDeciveCH());
                            Log.d(TAG, "status: " + statusItems.get(i).getStatus());
                            isAdd = true;


                        }
                    }
                    sceneCh = items.get(position).getDeciveCH();
                    switch (sceneCh) {
                        case "Scene 1":
                            sceneCh = "scene1";
                            break;
                        case "Scene 2":
                            sceneCh = "scene2";
                            break;
                        case "Scene 3":
                            sceneCh = "scene3";
                            break;
                        case "Scene 4":
                            sceneCh = "scene4";
                            break;
                        case "Scene 5":
                            sceneCh = "scene5";
                            break;
                        case "Scene 6":
                            sceneCh = "scene6";
                            break;
                    }
                    command = deviceCommand.MaxScene(UDP.hexToByte(items.get(position).getHostDeviceSN()), sceneCh);
                    sendCommand(command);
                   /* for (SceneStatus status : statusItems) {
                        if (status.getDeviceSN().equals(items.get(position).getHostDeviceSN())) {
                            isAdd = true;
                        } else {
                            isAdd = false;
                        }
                    }*/
                    if (!isAdd) {
                        SceneStatus s = new SceneStatus();
                        s.setDeviceSN(items.get(position).getHostDeviceSN());
                        s.setStatus(items.get(position).getDeciveCH());
                        statusItems.add(s);
                    }

                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView itemNameText;
            TextView itemChText;
            ImageView itemIconView;
            Button bottomBtn;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                itemNameText = itemView.findViewById(R.id.item_name);
                itemChText = itemView.findViewById(R.id.ch_text);
                itemIconView = itemView.findViewById(R.id.shortcut_icon);
                bottomBtn = itemView.findViewById(R.id.buttom_btn);
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
                    Log.d(TAG, "onMainReceive: " + hex);


                    /**確保收到的回傳值正確再進行SN擷取*/
                    if (hex.contains("1B0B")) {
                      /*  new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                loadingDialog.dismissDialog();
                            }
                        },300);*/

                        maxSceneSN = hex.substring(4, 16); //取得AP模式 M'S的SN

                        Log.d(TAG, "MSWiFiSN: " + maxSceneSN);
                     /*   byte[] searchAllDeviceDefault = {0x0b, 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, 0x19, 0x30, 0x02, 0x08, 0x07};
                        String searchAllDeviceCustom = UDP.byteArrayToHexStr(searchAllDeviceDefault);
                        searchAllDeviceCustom = searchAllDeviceCustom.replace("000000000007", maxSceneSN); //將Commend中的SN換成MSWiFiSN
                        // Log.d(TAG, "SearchCMD: "+searchAllDeviceCustom);
                        searchAllDevice = UDP.hexToByte(searchAllDeviceCustom);*/
                    }


                    //確定收到資料是所有裝置狀態
                    if (resultBytes.length > 0) {
                        if (!hex.contains("1B0B") && !hex.contains("0B12") && !hex.contains("0B13") && (resultBytes.length - 1) % 32 == 0) {
                            int deviceNum = Integer.parseInt(hex.substring(0, 2), 16);
                            Log.d(TAG, "陣列長度: " + resultBytes.length);
                            //判斷第一個byte的數字是否代表長度 否則可能閃退
                            if (deviceNum == (resultBytes.length - 1) / 32) {
                                Log.d(TAG, "數量: " + deviceNum);
                                // && (resultBytes.length-1)%32==0
                                sceneDeviceList = new ArrayList<>();
                                for (int i = 1; i <= deviceNum; i++) {
                                    //扣掉長度byte後 每32個bytes切成一個裝置
                                    byte[] device = Arrays.copyOfRange(resultBytes, 1 + (i - 1) * 32, i * 32);
                                    Log.d(TAG, "裝置" + i + ": " + UDP.byteArrayToHexStr(device));
                                    if (device[15] == (byte) 0x0b) {
                                        String status = "";
                                        SceneStatus sceneDevice = new SceneStatus();
                                        sceneDevice.setDeviceSN(UDP.byteArrayToHexStr(device).substring(32, 44));
                                        switch (UDP.byteArrayToHexStr(device).substring(0, 2)) {
                                            case "16":
                                                status = "Scene 1";
                                                break;
                                            case "18":
                                                status = "Scene 2";
                                                break;
                                            case "1A":
                                                status = "Scene 3";
                                                break;
                                            case "1C":
                                                status = "Scene 4";
                                                break;
                                            case "1E":
                                                status = "Scene 5";
                                                break;
                                            case "20":
                                                status = "Scene 6";
                                                break;
                                            case "98":
                                                status = "power";
                                                break;
                                        }
                                        sceneDevice.setStatus(status);
                                        sceneDeviceList.add(sceneDevice);
                                        // Log.d(TAG, "sceneDevice: "+sceneDeviceList);
                                    }
                                    //OneTouch狀態判定
                                    if (device[15] == (byte) 0x0a) {
                                        hasOneTouch = true;
                                        if(device[0]== 0x03){
                                            oneTouchStatus = true;
                                            Log.d(TAG, "oneTouch: "+oneTouchStatus);
                                        }
                                        if(device[0]== 0x00){
                                            oneTouchStatus = false;
                                            Log.d(TAG, "oneTouch: "+oneTouchStatus);
                                        }

                                    }
                                }
                                //收到指令後將畫面更新
                                mainRecyclerAdapter.notifyDataSetChanged();


                            }
                        }
                    }
                    String sceneSn = "";
                    String type = "";
                    // String scene = String.format("%02X ", 0x0b);


                    //取得系統密碼
                    if (hexToAscii(hex).contains("MAC")) {

                        systemMAC = "MS"+hexToAscii(hex).substring(3, 5)+hexToAscii(hex).substring(6, 8)+hexToAscii(hex).substring(9, 11)+hexToAscii(hex).substring(12,14)+hexToAscii(hex).substring(15,17)+hexToAscii(hex).substring(18,20);
                       systemMAC = systemMAC.toUpperCase();
                        Log.d(TAG, "MAC: "+systemMAC);
                        //MQTT連接
                        if(!connectMqtt) {
                            MqttManager.USERNAME = getRandomString(8);
                            m_mqttManager = MqttManager.getInstance(MainActivity.this);
                            //MqttManager.upgrade_topic = "max-system/V4/" + systemMAC + "/upgrade";
                            Log.d(TAG, "mqtt: "+MqttManager.upgradeString);
                            MqttManager.progress_topic = "max-system/V4/" + systemMAC + "/progress";
                            MqttManager.status_topic = "max-system/V4/" + systemMAC + "/status";
                            MqttManager.upgrade_topic = "max-system/V4/" + systemMAC + "/upgrade";
                            MqttManager.version_TOPIC = "max-system/V4/" + systemMAC + "/version";
                            MqttManager.update_topic = "max-system/V4/" + systemMAC + "/update";
                            String clientID = getRandomString(8);
                            Log.d(TAG, "client id: "+clientID);
                            Log.d(TAG, "userName: "+ MqttManager.USERNAME);
                            m_mqttManager.connect(clientID, mHandler);
                            connectMqtt = true;
                        }
                        //正規表示式取得密碼 regex
                        Matcher matcher;
                        matcher = Pattern.compile("PWD([A-Za-z0-9]{6,8})").matcher(hexToAscii(hex));
                        if (matcher.find()) {
                            systemPassword = matcher.group();
                            systemPassword = systemPassword.substring(3);
                            Log.d(TAG, "password: " + systemPassword);
                            if(userPassword.equals(systemPassword)){
                                systemEnter = true;
                            }

                            if (showPassword && !userPassword.equals(systemPassword)) {
                                showPassword = false;
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                final EditText editText = new EditText(MainActivity.this); //final一個editText
                                final TextView info = new TextView(MainActivity.this); //final一個editText
                                builder.setView(editText);
                                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(8)});
                                builder.setTitle("Warning");
                                builder.setCancelable(false);
                                builder.setMessage("Please enter the System password to continue.");
                                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Boolean wantToCloseDialog = false;


                                        if(systemPassword.equals(editText.getText().toString())) {
                                            getSharedPreferences("Data", Context.MODE_PRIVATE)
                                                    .edit()
                                                    .putString("userPassword", editText.getText().toString())
                                                    .commit();
                                            systemEnter = true;
                                            wantToCloseDialog = true;
                                        } else {
                                            StyleableToast toast = StyleableToast.makeText(MainActivity.this, "Wrong Password.", Toast.LENGTH_SHORT, R.style.exampleToast);
                                            toast.show();
                                        }
                                        //Do stuff, possibly set wantToCloseDialog to true then...
                                        if(wantToCloseDialog)
                                            dialog.dismiss();
                                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                                    }
                                });

                                //  Log.d(TAG, "MAC: " + systemMAC);
                            }
                        }

                    }
                    //取得版本資訊
                    if (hexToAscii(hex).contains("MSW")) {

                        systemVersion = hexToAscii(hex).substring(3, 4)+"."+hexToAscii(hex).substring(4);
                        Log.d(TAG, "Version: " + systemVersion);

                    }

                    //根據scene的狀態顯示捷徑該亮的
                    if(!controlling) {
                        if (resultBytes.length == 25) {
                            sceneSn = hex.substring(32, 44);
                            type = hex.substring(30, 32);
                            Log.d(TAG, "type: " + type.equals("0B"));
                            if (resultBytes[0] == 0x16 && type.equals("0B")) {

                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 1");
                                        mainRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                            if (resultBytes[0] == 0x18 && type.equals("0B")) {
                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 2");
                                        mainRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                            if (resultBytes[0] == 0x1A && type.equals("0B")) {
                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 3");
                                        mainRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                            if (resultBytes[0] == 0x1C && type.equals("0B")) {
                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 4");
                                        mainRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                            if (resultBytes[0] == 0x1E && type.equals("0B")) {
                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 5");
                                        mainRecyclerAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "2 ");
                                    }
                                }
                            }
                            if (resultBytes[0] == 0x20 && type.equals("0B")) {
                                for (int i = 0; i < sceneDeviceList.size(); i++) {
                                    if (sceneDeviceList.get(i).getDeviceSN().equals(sceneSn)) {
                                        sceneDeviceList.get(i).setStatus("Scene 6");
                                        mainRecyclerAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "1 ");
                                    }
                                }
                            }
                            if (resultBytes[0] == 0x00 && type.equals("0A")) {
                                oneTouchStatus = false;
                                mainRecyclerAdapter.notifyDataSetChanged();
                            }
                            if (resultBytes[0] == 0x03 && type.equals("0A")) {
                                oneTouchStatus = true;
                                mainRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    //判斷回傳指令是所有裝置的指令

                    /*if (resultBytes != null && maxSceneSN != null && addcount == 0) {
                        if (resultBytes.length % 7 == 1 && (resultBytes.length - 1) / 7 == resultBytes[0]) {
                            allDeviceResult = resultBytes;
                            // Log.d(TAG, "長度: "+resultBytes.length);
                            Log.d(TAG, "all maxScene: " + hex);
                            addcount += 1;

                            //  addToDataBase();
                        }

                    }*/
                    break;

            }
        }
        /*private void addToDataBase() {
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
                        deviceCheckData = DataBase.getInstance(MainActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(MainActivity.this).getDataUao().insertData(device);
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
                        deviceCheckData = DataBase.getInstance(MainActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(MainActivity.this).getDataUao().insertData(device);
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
                        deviceCheckData = DataBase.getInstance(MainActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(MainActivity.this).getDataUao().insertData(device);
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
                        deviceCheckData = DataBase.getInstance(MainActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(MainActivity.this).getDataUao().insertData(device);
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
                        deviceCheckData = DataBase.getInstance(MainActivity.this).getDataUao().findDataBySNandID(device.getDeviceId(), device.getDeviceSN());
                        if (deviceCheckData.size() == 0) {
                            DataBase.getInstance(MainActivity.this).getDataUao().insertData(device);
                        }

                    }).start();
                }
            }

        }*/


    }
   /* public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }*/


}
