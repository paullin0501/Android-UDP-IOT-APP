package com.rexlite.rexlitebasicnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;
import com.rexlite.rexlitebasicnew.SceneShortcut.MoreActionActivity;
import com.rexlite.rexlitebasicnew.SceneShortcut.SceneShortcutSettingActivity;
import com.rexlite.rexlitebasicnew.SceneShortcut.SceneSleepingModeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.rexlite.rexlitebasicnew.LocalIP.getLocalIP;

public class SceneSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "scenesetting";
    private Button[] btn = new Button[7];
    SceneSettingActivity.MyBroadcast myBroadcast = new SceneSettingActivity.MyBroadcast();
    private Button btn_unfocus;
    //UDP udpServer;
    ExecutorService exec = Executors.newCachedThreadPool();
    DeviceCommand deviceCommand = new DeviceCommand();
    List<Shortcut> shortcutData;
    byte[] maxSceneSN;//點擊該裝置時傳送對應的SN
    byte[] statusData;//目前裝置的實際狀態
    byte maxType = 0x0b;
    String deviceName;
    boolean showDialog = true;//顯示錯誤訊息用
    private int[] btn_id = {R.id.scene1_btn, R.id.scene2_btn, R.id.scene3_btn, R.id.scene4_btn, R.id.scene5_btn, R.id.scene6_btn, R.id.power_btn, R.id.up_btn, R.id.down_btn};
    private int[] btn_offimage = {R.drawable.scene1, R.drawable.scene2, R.drawable.scene3, R.drawable.scene4, R.drawable.scene5, R.drawable.scene6, R.drawable.scene_power};
    static View loadingAni;
    View parent;
    int failedCount = 0;
    boolean isUsed = false; //如果在操作中就不更新狀態避免跳動
    Runnable runnable; //計時器
    Handler handler = new Handler(); //計時器
    //長按設定變數
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private boolean isShortcut = true;
    Shortcut shortcut = new Shortcut();
    private RadioGroup mGroup1;
    private RadioGroup mGroup2;
    private RadioGroup mGroup3;
    private List<Shortcut> maxSceneShortcutList; //當前頁面的捷徑資料
    private MenuItem nowItem;
    //情境面板狀態相關參數
    private String lockStatus = "00"; //判斷面板是否鎖定
    private String copyStatus = "00";
    private String groupStatus = "00";
    private String copySN;
    private Device copyDevice;
    private static TextView lockText;
    private static TextView statusText;
    private static ImageView lockImg;
    private Device currentDevice;
    private boolean sceneStatus; //判斷是否在情境鍵

    String subtitleText;
    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(SceneSettingActivity.this);
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
        if (event.getUpgrade().equals("0") && finishUpdate) {
            loadingDialog.dismissDialog();
            finishUpdate = false;
        }
        if (event.getUpgrade().equals("1")) {
            loadingDialog.startLoadingDialog();
            loadingDialog.setLoadingText("Firmware is updating...");
            finishUpdate = true;
        }
        //根據mqtt的回傳值來顯示更新的UI


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(SceneSettingActivity.this);
    }


    //新增及綁定長按視窗
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        String title = "";
        menuInflater.inflate(R.menu.floating_menu, menu);
        Log.d(TAG, "onCreateContextMenu: " + menu.findItem(R.id.float_shortcut));
        switch (v.getId()) {
            case R.id.scene1_btn:
                title = "Scene 1";
                break;
            case R.id.scene2_btn:
                title = "Scene 2";
                break;
            case R.id.scene3_btn:
                title = "Scene 3";
                break;
            case R.id.scene4_btn:
                title = "Scene 4";
                break;
            case R.id.scene5_btn:
                title = "Scene 5";
                break;
            case R.id.scene6_btn:
                title = "Scene 6";
                break;

        }
        menu.setHeaderTitle(title);
        //先檢查是否有加入過捷徑
        String finalTitle = title;
        new Thread(() -> {
            shortcutData = ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN), finalTitle, String.format("%02X ", maxType), true);
            //取得選擇當筆捷徑的資料
            maxSceneShortcutList = ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().findByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN), finalTitle, String.format("%02X ", maxType));
            if(maxSceneShortcutList.size()>0) {
                shortcut = maxSceneShortcutList.get(0);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (shortcutData.size() > 0) {
                        MenuItem item = menu.findItem(R.id.float_shortcut);
                        item.setTitle("Remove from shortcut");
                        isShortcut = true;
                    }
                    if (shortcutData.size() == 0) {
                        MenuItem item = menu.findItem(R.id.float_shortcut);
                        item.setTitle("Add to shortcut");
                        isShortcut = false;
                    }
                }
            });
        }).start();
        /*menu.add(0, v.getId(), 1, "Add to favorites");
        Log.d(TAG, "onCreateContextMenu: "+menu.findItem(3));*/
      /* MenuItem item = menu.getItem(3);
        Log.d(TAG, "onCreateContextMenu: "+item.getTitle());
       if(item!=null) {
           item.setTitle("Remove from shortcut");
           Log.d(TAG, "hhh: ");
       }*/
//        item.setTitle("Remove from shortcut");

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.float_sleeping:
                settingSleepingMode();
                break;
            case R.id.float_shortcut:
                addOrRemoveShortcut();
                break;
            case R.id.float_icon:
                settingIcon();
                break;
            case R.id.float_rename:
                settingRename();
                break;
        }
        return true;
    }

    public void settingSleepingMode() {

        dialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        final View sleepingPopupView = getLayoutInflater().inflate(R.layout.scene_sleeping_popup, null);
        //ok按鍵設定
        final Button ok_btn = sleepingPopupView.findViewById(R.id.ok_button);
        dialogBuilder.setView(sleepingPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedText = null;
                String selectedText2 = null;
                byte[] chData = null;//指令(還不包括亮度與SN)
                String timeText = null;
                RadioGroup radioGroup = (RadioGroup) sleepingPopupView.findViewById(R.id.radio_group_1);
                RadioGroup radioGroup2 = (RadioGroup) sleepingPopupView.findViewById(R.id.radio_group_2);
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
                    if (radioButton2.getId() == min_btn.getId()) {
                        selectedText2 = "0.5 hour";
                        timeText = "min sleep";
                        chData = settingSceneSleepingModeCMD(maxSceneSN, timeText);
                    }
                    if (radioButton2.getId() == hour_btn.getId()) {
                        selectedText2 = "1 hour";
                        timeText = "hour sleep";
                        chData = settingSceneSleepingModeCMD(maxSceneSN, timeText);
                    }
                }
                if (radioButton.getId() == bright_btn.getId()) {
                    selectedText = "bright";
                    if (radioButton2.getId() == min_btn.getId()) {
                        selectedText2 = "0.5 hour";
                        timeText = "min bright";
                        chData = settingSceneSleepingModeCMD(maxSceneSN, timeText);
                    }
                    if (radioButton2.getId() == hour_btn.getId()) {
                        selectedText2 = "1 hour";
                        timeText = "hour bright";
                        chData = settingSceneSleepingModeCMD(maxSceneSN, timeText);
                    }

                }
                maxSceneCommand(chData);
                Toast.makeText(SceneSettingActivity.this, selectedText2 + "to " + selectedText, Toast.LENGTH_SHORT).show();
            /*    Intent intent = new Intent(SceneSettingActivity.this, SceneSettingActivity.class);
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
                },100);*/
                dialog.dismiss();
            }
        });
    }

    public void addOrRemoveShortcut() {

        if (!isShortcut) {
            new Thread(() -> {
                //加入預設icon
                if (shortcut.getIcon() == 0) {
                    shortcut.setIcon(R.drawable.scene_icon1_on);
                    ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().updateShowandicon(shortcut.getId(), shortcut.getIcon(), true);
                } else {

                    ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().updateMaxSceneShow(shortcut.getId(), true);
                }
            }).start();
            Toast.makeText(SceneSettingActivity.this, "Add to shortcut successful", Toast.LENGTH_SHORT).show();
            isShortcut = true;
        } else {
            new Thread(() -> {
                ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().updateMaxSceneShow(shortcut.getId(), false);
            }).start();
            Toast.makeText(SceneSettingActivity.this, "Remove from shortcut successful", Toast.LENGTH_SHORT).show();
            isShortcut = false;

        }


    }

    public void settingIcon() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.scene_icon_popup, null);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
        Button ok_btn = popupView.findViewById(R.id.ok_button);
        mGroup1 = popupView.findViewById(R.id.radio_group_1);
        mGroup2 = popupView.findViewById(R.id.radio_group_2);
        mGroup3 = popupView.findViewById(R.id.radio_group_3);
        if (shortcut.getIcon() == 0) {
            shortcut.setIcon(R.drawable.scene_icon1_on);
        }
        handleStartUi(shortcut.getIcon());
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().updateMaxSceneOnlyIcon(shortcut.getIcon(), shortcut.getId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }).start();
            }
        });


    }


    public void settingRename() {
       /* dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.scene_rename_popup,null);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(SceneSettingActivity.this); //final一個editText
        builder.setView(editText);
        editText.setText(shortcut.getName());
        builder.setTitle("Rename");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(() -> {
                    String updateText;
                    updateText = editText.getText().toString();
                    shortcut.setName(updateText);
                    ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().updateMaxSceneOnlyName(shortcut.getName(), shortcut.getId());
                }).start();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }


    //設定icon按鈕為單選
    public void handleCombinedClick(View view) {
        // Clear any checks from both groups:
        mGroup1.clearCheck();
        mGroup2.clearCheck();
        mGroup3.clearCheck();

        // Manually set the check in the newly clicked radio button:
        ((RadioButton) view).setChecked(true);

        // Perform any action desired for the new selection:
        switch (view.getId()) {
            case R.id.icon1_btn:
                shortcut.setIcon(R.drawable.scene_icon1_on);
                break;

            case R.id.icon2_btn:
                shortcut.setIcon(R.drawable.scene_icon2_on);
                break;
            case R.id.icon3_btn:
                shortcut.setIcon(R.drawable.scene_icon3_on);
                break;

            case R.id.icon4_btn:
                shortcut.setIcon(R.drawable.scene_icon4_on);
                break;

            case R.id.icon5_btn:
                shortcut.setIcon(R.drawable.scene_icon5_on);
                break;
            case R.id.icon6_btn:
                shortcut.setIcon(R.drawable.scene_icon6_on);
                break;

            case R.id.icon7_btn:
                shortcut.setIcon(R.drawable.scene_icon7_on);
                break;
            case R.id.icon8_btn:
                shortcut.setIcon(R.drawable.scene_icon8_on);
                break;
            case R.id.icon9_btn:
                shortcut.setIcon(R.drawable.scene_icon9_on);
                break;

        }
    }

    public void handleStartUi(int icon) {
        // Clear any checks from both groups:
        mGroup1.clearCheck();
        mGroup2.clearCheck();
        mGroup3.clearCheck();
      /*  // Manually set the check in the newly clicked radio button:
        ((RadioButton) view).setChecked(true);*/

        // Perform any action desired for the new selection:
        switch (icon) {
            case R.drawable.scene_icon1_on:
                shortcut.setIcon(R.drawable.scene_icon1_on);
                mGroup1.check(R.id.icon1_btn);
                break;

            case R.drawable.scene_icon2_on:
                shortcut.setIcon(R.drawable.scene_icon2_on);
                mGroup1.check(R.id.icon2_btn);
                break;
            case R.drawable.scene_icon3_on:
                shortcut.setIcon(R.drawable.scene_icon3_on);
                mGroup1.check(R.id.icon3_btn);
                break;

            case R.drawable.scene_icon4_on:
                shortcut.setIcon(R.drawable.scene_icon4_on);
                mGroup2.check(R.id.icon4_btn);
                break;

            case R.drawable.scene_icon5_on:
                shortcut.setIcon(R.drawable.scene_icon5_on);
                mGroup2.check(R.id.icon5_btn);
                break;
            case R.drawable.scene_icon6_on:
                shortcut.setIcon(R.drawable.scene_icon6_on);
                mGroup2.check(R.id.icon6_btn);
                break;

            case R.drawable.scene_icon7_on:
                shortcut.setIcon(R.drawable.scene_icon7_on);
                mGroup3.check(R.id.icon7_btn);
                break;
            case R.drawable.scene_icon8_on:
                shortcut.setIcon(R.drawable.scene_icon8_on);
                mGroup3.check(R.id.icon8_btn);
                break;
            case R.drawable.scene_icon9_on:
                shortcut.setIcon(R.drawable.scene_icon9_on);
                mGroup3.check(R.id.icon9_btn);
                break;

        }
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
        byte[] MaxScene_Temp = deviceCommand.ConcatAll(sceneCMD, SN, DATATemp, timeCMD, lastTemp);
        byte[] CRCTemp = deviceCommand.GetCRC(MaxScene_Temp);
        byte[] SendCMD = deviceCommand.ConcatAll(MaxScene_Temp, CRCTemp);
        return SendCMD;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_setting);
        Intent intent = getIntent();

        TextView subtitle = findViewById(R.id.subtitle);
        Bundle info = intent.getExtras();
        maxSceneSN = info.getByteArray("maxSceneSN");
        deviceName = info.getString("deviceName");
        subtitleText = info.getString("deviceSubtitle", "");
        subtitle.setText(subtitleText);
        Button up = findViewById(R.id.up_btn);
        Button down = findViewById(R.id.down_btn);
        parent = findViewById(R.id.card_layout);
        statusText = findViewById(R.id.status_text);
        lockText = findViewById(R.id.lock_text);
        lockImg = findViewById(R.id.lock_image);
        new Thread(() -> {
            List<Device> data = DataBase.getInstance(SceneSettingActivity.this).getDataUao().findDataBySNandID("0b", UDP.byteArrayToHexStr(maxSceneSN));
            currentDevice = data.get(0);
            Log.d(TAG, "currentDevice: " + currentDevice.getDeviceName());
        }).start();
        Rect delegateArea = new Rect();
      /*  for (int i = 1; i < btn.length-2; i++) {
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].getHitRect(delegateArea);
            delegateArea.top -= 100;    // increase top hit area
            delegateArea.left -= 100;   // increase left hit area
            delegateArea.bottom += 100; // increase bottom hit area
            delegateArea.right += 100;  // increase right hit area
            TouchDelegate expandedArea = new TouchDelegate(delegateArea, btn[i]);
            // give the delegate to an ancestor of the view we're
            // delegating the
            // area to
            Log.d(TAG, "onCreate: 111");
            if (View.class.isInstance(btn[i].getParent())) {
                ((View) btn[i].getParent())
                        .setTouchDelegate(expandedArea);
                Log.d(TAG, "onCreate: 111");
            }
        }*/

   /*     Button button1 = findViewById(R.id.scene1_btn);
        button1.getHitRect(delegateArea);
        delegateArea.top -= 100;    // increase top hit area
        delegateArea.left -= 100;   // increase left hit area
        delegateArea.bottom += 1000; // increase bottom hit area
        delegateArea.right += 1000;  // increase right hit area
        TouchDelegate expandedArea = new TouchDelegate(delegateArea, button1);
        if (View.class.isInstance(button1.getParent())) {
            ((View) button1.getParent())
                    .setTouchDelegate(expandedArea);
            Log.d(TAG, "onCreate: 111");
        }*/
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte command[];
                command = deviceCommand.MaxScene(maxSceneSN, "up");
                Log.d(TAG, "onClick: " + "up");
                maxSceneCommand(command);
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte command[];
                command = deviceCommand.MaxScene(maxSceneSN, "down");
                Log.d(TAG, "onClick: " + "down");
                maxSceneCommand(command);
            }
        });
        //取得動畫View
        loadingAni = findViewById(R.id.spin_kit);

        Log.d("scenesetting", "onCreate: " + byteArrayToHexStr(maxSceneSN));
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        ImageView detailIcon = findViewById(R.id.detail_icon);
        TextView title = findViewById(R.id.toolbar_title);
        //返回按鈕
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SceneSettingActivity.this, Device3Activity.class);
                intent.putExtra("id", 4);
                startActivity(intent);
                Animatoo.animateSlideRight(SceneSettingActivity.this);
            }
        });
        detailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SceneSettingActivity.this, MoreActionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN", maxSceneSN);
                bundle.putString("deviceName", deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(SceneSettingActivity.this);
            }
        });
        title.setText(deviceName);
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(SceneSettingActivity.this, DeviceNameSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN", maxSceneSN);
                bundle.putString("deviceName", deviceName);
                bundle.putString("deviceCH", "0");
                bundle.putString("deviceSubtitle", subtitleText);
                bundle.putInt("page", 4);
                String type = String.format("%02X ", maxType);
                bundle.putString("type", "0b");
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(SceneSettingActivity.this);
                return false;
            }
        });
        //連線相關
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        // connectUDP();
        //當一進到畫面時 先了解裝置目前的狀態
        byte[] command = deviceCommand.MaxScene(maxSceneSN, "status");
        Log.d(TAG, "狀態指令: " + byteArrayToHexStr(command));
        maxSceneCommand(command);
        //當裝置沒有回應時  過3秒後重新發送指令
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (statusData == null) {
                    failedCount = 2;
                    Log.d(TAG, "failed: " + failedCount);
                    maxSceneCommand(command);
                }
            }
        }, 3000);
        settingShortcut();
        //單選按鈕邏輯設定
        for (int i = 0; i < btn.length; i++) {
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].setBackgroundResource(btn_offimage[i]);
            if(i<6) {
                registerForContextMenu(btn[i]);
            }
            btn[i].setOnClickListener(this);
        }
        //設定計時設定是否處在使用者操作的狀態
        runnable = new Runnable() {
            @Override
            public void run() {
                isUsed = false;
                handler.postDelayed(this, 1500);
            }
        };
        runnable.run(); //執行計時器
        btn_unfocus = btn[0];
        setBtnDisable();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
        EventBus.getDefault().unregister(this);
    }

    /* @Override
     protected void onResume() {
         super.onResume();
         byte[] command = deviceCommand.MaxScene(maxSceneSN, "status");
         maxSceneCommand(command);
     }*/
    public void setBtnDisable() {
        for (int i = 0; i < btn.length; i++) {
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].setEnabled(false);
        }

    }

    public void setBtnEnable() {
        for (int i = 0; i < btn.length; i++) {
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].setEnabled(true);
        }

    }

    private void settingShortcut() {
        new Thread(() -> {
            shortcutData = ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().findDeviceSNandDeviceType(UDP.byteArrayToHexStr(maxSceneSN), String.format("%02X ", maxType));
            if (shortcutData.size() == 0) {
                for (int i = 1; i < 7; i++) {
                    Shortcut shortcut = new Shortcut();
                    shortcut.setName("Scene " + i);
                    shortcut.setDeciveCH("Scene " + i);
                    shortcut.setType("0");
                    shortcut.setHostDeviceSN(UDP.byteArrayToHexStr(maxSceneSN));
                    shortcut.setDeviceType(String.format("%02X ", maxType));
                    shortcut.setHostDeviceName(deviceName);
                    ShortcutDataBase.getInstance(SceneSettingActivity.this).getShortcutDataDao().insertData(shortcut);
                }
            }
        }).start();


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

    /*  public void expandBtnArea(int id){
          parent.post(new Runnable() {
              @Override
              public void run() {
                  Rect delegateArea = new Rect();
                  btn[id].getHitRect(delegateArea);
                  delegateArea.top +=300;
                  delegateArea.bottom += 300;
                  TouchDelegate expandedArea = new TouchDelegate(delegateArea, btn[id]);
                  // give the delegate to an ancestor of the view we're
                  // delegating the
                  // area to
                  if (View.class.isInstance(btn[id].getParent())) {
                      ((View) btn[id].getParent())
                              .setTouchDelegate(expandedArea);
                  }
            *//*      for (int i = 0; i < btn.length; i++) {
                    btn[i] = (Button) findViewById(btn_id[i]);
                    btn[i].getHitRect(delegateArea);
                    delegateArea.top +=300;
                    delegateArea.bottom += 300;
                    TouchDelegate expandedArea = new TouchDelegate(delegateArea, btn[i]);
                    // give the delegate to an ancestor of the view we're
                    // delegating the
                    // area to
                    if (View.class.isInstance(btn[i].getParent())) {
                        ((View) btn[i].getParent())
                                .setTouchDelegate(expandedArea);
                    }
                }*//*


            }
        });
    }*/
    @Override
    public void onClick(View v) {
        byte command[];
        isUsed = true;
        switch (v.getId()) {
            case R.id.scene1_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene1");
                maxSceneCommand(command);
                scene1On();
                // expandBtnArea(0);
                break;

            case R.id.scene2_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene2");
                maxSceneCommand(command);
                scene2On();
                //expandBtnArea(1);
                break;

            case R.id.scene3_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene3");
                maxSceneCommand(command);
                scene3On();
                // expandBtnArea(2);
                break;

            case R.id.scene4_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene4");
                maxSceneCommand(command);
                scene4On();
                // expandBtnArea(3);
                break;
            case R.id.scene5_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene5");
                maxSceneCommand(command);
                scene5On();
                // expandBtnArea(4);
                break;
            case R.id.scene6_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "scene6");
                maxSceneCommand(command);
                scene6On();
                //expandBtnArea(5);
                break;
            case R.id.power_btn:

                if(sceneStatus){//00
                    command = deviceCommand.MaxScene(maxSceneSN, "powerOn");
                    maxSceneCommand(command);
                    powerOn();
                }
                if(!sceneStatus){//01
                    command = deviceCommand.MaxScene(maxSceneSN, "powerOff");
                    maxSceneCommand(command);
                }

                //expandBtnArea(6);
                break;
            case R.id.up_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "up");
                Log.d(TAG, "onClick: " + "up");
                maxSceneCommand(command);
                break;
            case R.id.down_btn:
                command = deviceCommand.MaxScene(maxSceneSN, "down");
                Log.d(TAG, "onClick: " + "down");
                maxSceneCommand(command);
                break;
        }
    }

    private void scene1On() {
        btn[0].setBackgroundResource(R.drawable.scene1_on);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void scene2On() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2_on);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void scene3On() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3_on);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void scene4On() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4_on);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void scene5On() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5_on);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void scene6On() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6_on);
        btn[6].setBackgroundResource(R.drawable.scene_power);
    }

    private void powerOn() {
        btn[0].setBackgroundResource(R.drawable.scene1);
        btn[1].setBackgroundResource(R.drawable.scene2);
        btn[2].setBackgroundResource(R.drawable.scene3);
        btn[3].setBackgroundResource(R.drawable.scene4);
        btn[4].setBackgroundResource(R.drawable.scene5);
        btn[5].setBackgroundResource(R.drawable.scene6);
        btn[6].setBackgroundResource(R.drawable.scene_poweron);

    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //回傳接收資料處理
    private class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            assert mAction != null;
            switch (mAction) {
                /**接收來自UDP回傳之訊息*/
                case UDP.RECEIVE_ACTION:
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
                    if (resultBytes[0] != 0x16 && resultBytes[0] != 0x18 && resultBytes[0] != 0x1A && resultBytes[0] != 0x1C && resultBytes[0] != 0x1E && resultBytes[0] != 0x20 && resultBytes[0] != (byte) 0x98 && showDialog && failedCount == 2) {
                        Log.d(TAG, "MaxScene沒有回應 確認是不是在同一個WIFI網路");
                        showDialog = false;
                        loadingAni.setVisibility(View.GONE);
                        new AlertDialog.Builder(SceneSettingActivity.this)
                                .setTitle("裝置沒有回應")
                                .setMessage("裝置沒有回應，請確認手機是否與裝置在同一個WiFi")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    if ((hex.length() == 50 || hex.length() ==100) && maxSceneSN != null && UDP.byteArrayToHexStr(maxSceneSN).equals(hex.substring(32, 44))){
                        lockStatus = hex.substring(4, 6);
                        copyStatus = hex.substring(6, 8);
                        copySN = hex.substring(10, 22);
                        groupStatus = hex.substring(22, 24);

                      /*  Log.d(TAG, "是否鎖定(00鎖定):  " + lockStatus);
                        Log.d(TAG, "copyStatus:  " + copyStatus);
                        Log.d(TAG, "copySN: " + copySN);
                        Log.d(TAG, "groupStatus: " + groupStatus);*/
                        //Log.d(TAG, "sn: " + UDP.byteArrayToHexStr(maxSceneSN).equals(hex.substring(32, 44)));
                        //情境面板鎖定判斷
                        if (lockStatus.equals("00")) {
                            lockText.setVisibility(View.VISIBLE);
                            lockImg.setVisibility(View.VISIBLE);
                        }

                        if (lockStatus.equals("FF")) {
                            lockText.setVisibility(View.GONE);
                            lockImg.setVisibility(View.GONE);
                        }
                        //複製狀態判定
                        if (copyStatus.equals("00") && groupStatus.equals("00")) {
                            statusText.setVisibility(View.GONE);
                            new Thread(() -> {
                                DataBase.getInstance(SceneSettingActivity.this).getDataUao().updateDeviceSuperior("", currentDevice.getDeviceId(), currentDevice.getDeviceSN());
                            }).start();
                        } else {
                            statusText.setVisibility(View.VISIBLE);
                            if (!copyStatus.equals("00")) {
                                if (copyDevice == null) {
                                    if (!copySN.equals(currentDevice.getDeviceSN())) {
                                        new Thread(() -> {
                                            List<Device> data = DataBase.getInstance(SceneSettingActivity.this).getDataUao().findDataBySNandID("0b", copySN);
                                            if (data.size() > 0) {
                                                copyDevice = data.get(0);

                                                runOnUiThread(() -> {
                                                    statusText.setText("The device is copied from M'S " + copyDevice.getDeviceName());
                                                });
                                                DataBase.getInstance(SceneSettingActivity.this).getDataUao().updateDeviceSuperior(copyDevice.getDeviceName(), currentDevice.getDeviceId(), currentDevice.getDeviceSN());
                                                DataBase.getInstance(SceneSettingActivity.this).getDataUao().updateDeviceSuperior(currentDevice.getDeviceName(), copyDevice.getDeviceId(), copyDevice.getDeviceSN());
                                            }
                                        }).start();
                                    }
                                    if (copySN.equals(currentDevice.getDeviceSN())) {
                                        new Thread(() -> {
                                            List<Device> data = DataBase.getInstance(SceneSettingActivity.this).getDataUao().findDataBySNandID("0b", currentDevice.getDeviceSN());
                                            currentDevice = data.get(0);
                                            runOnUiThread(() -> {
                                                statusText.setText("The device is copied from M'S " + currentDevice.getSuperior());
                                            });
                                        }).start();
                                    }
                                }
                            }
                            //群組狀態判定
                            if (groupStatus.equals("01")) {
                                if (copyDevice == null) {
                                    if (!copySN.equals(currentDevice.getDeviceSN())) {
                                        new Thread(() -> {
                                            List<Device> data = DataBase.getInstance(SceneSettingActivity.this).getDataUao().findDataBySNandID("0b", copySN);
                                            copyDevice = data.get(0);
                                            runOnUiThread(() -> {
                                                statusText.setText("The device is grouped with M'S " + copyDevice.getDeviceName());
                                            });
                                            DataBase.getInstance(SceneSettingActivity.this).getDataUao().updateDeviceSuperior(copyDevice.getDeviceName(), currentDevice.getDeviceId(), currentDevice.getDeviceSN());
                                            DataBase.getInstance(SceneSettingActivity.this).getDataUao().updateDeviceSuperior(currentDevice.getDeviceName(), copyDevice.getDeviceId(), copyDevice.getDeviceSN());
                                        }).start();
                                    }
                                    if (copySN.equals(currentDevice.getDeviceSN())) {
                                        new Thread(() -> {
                                            List<Device> data = DataBase.getInstance(SceneSettingActivity.this).getDataUao().findDataBySNandID("0b", currentDevice.getDeviceSN());
                                            currentDevice = data.get(0);
                                            runOnUiThread(() -> {
                                                statusText.setText("The device is grouped with M'S " + currentDevice.getSuperior());
                                            });
                                        }).start();
                                    }
                                }
                            }


                        }

                        //根據狀態顯示畫面
                        if (!isUsed ) {
                            //&& UDP.byteArrayToHexStr(maxSceneSN).equals(hex.substring(32, 44)
                            if (resultBytes[0] == 0x16) {
                                scene1On();
                                statusData = resultBytes;
                                sceneStatus = true;
                                loadingAni.setVisibility(View.GONE);
                                setBtnEnable();
                            }
                            if (resultBytes[0] == 0x18) {
                                scene2On();
                                statusData = resultBytes;
                                sceneStatus = true;
                                loadingAni.setVisibility(View.GONE);
                                setBtnEnable();
                            }
                            if (resultBytes[0] == 0x1A) {
                                scene3On();
                                statusData = resultBytes;
                                loadingAni.setVisibility(View.GONE);
                                sceneStatus = true;
                                setBtnEnable();
                            }
                            if (resultBytes[0] == 0x1C) {
                                scene4On();
                                statusData = resultBytes;
                                loadingAni.setVisibility(View.GONE);
                                sceneStatus = true;
                                setBtnEnable();
                            }
                            if (resultBytes[0] == 0x1E) {
                                scene5On();
                                statusData = resultBytes;
                                loadingAni.setVisibility(View.GONE);
                                sceneStatus = true;
                                setBtnEnable();
                            }
                            if (resultBytes[0] == 0x20) {
                                scene6On();
                                statusData = resultBytes;
                                loadingAni.setVisibility(View.GONE);
                                sceneStatus = true;
                                setBtnEnable();
                            }
                            if (resultBytes[0] == (byte) 0x98) { //98要加(byte才會判斷正確)
                                powerOn();
                                statusData = resultBytes;
                                sceneStatus = false;
                                loadingAni.setVisibility(View.GONE);
                                setBtnEnable();
                            }
                        }
                        if (resultBytes[0] == 0x16) {
                            scene1On();
                            statusData = resultBytes;
                            sceneStatus = true;
                            loadingAni.setVisibility(View.GONE);
                            setBtnEnable();
                        }
                        if (resultBytes[0] == 0x16 || resultBytes[0] == 0x18 || resultBytes[0] == 0x1A || resultBytes[0] == 0x1C || resultBytes[0] == 0x1E || resultBytes[0] == 0x20) {
                            sceneStatus = true;
                        }
                        if (resultBytes[0] == (byte) 0x98) { //98要加(byte才會判斷正確)
                            sceneStatus = false;
                        }
                    }
                    break;

            }
        }
    }

}


