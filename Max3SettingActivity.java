package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class Max3SettingActivity extends AppCompatActivity {
    private static final String TAG = "max3setting";
    FragmentTransaction fragmentTransaction;
    Max3SettingActivity.MyBroadcast myBroadcast = new Max3SettingActivity.MyBroadcast();
    byte[] deviceSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    List<Shortcut> shortcutData;
    String deviceCh;
    int deviceChNum=1; //預設CH(沒有從捷徑進入時)
    String chInfo; //用來判斷裝置的種類
    byte[] statusCommand; //裝置目前的狀態
    byte maxType = 0x18; //設定控制的裝置類型 16為3切
    String ch1Type;
    String ch2Type;
    String ch3Type;
    String ch1CurtainType;
    String ch2CurtainType;
    String ch3CurtainType;
    String ch1Brightness;
    String ch1Temperature;
    String ch2Brightness;
    String ch2Temperature;
    String ch3Brightness;
    String ch3Temperature;
    ImageView ch1Up;
    ImageView ch1Down;
    ImageView ch2Up;
    ImageView ch2Down;
    ImageView ch3Up;
    ImageView ch3Down;
    boolean showDialog = true;//顯示錯誤訊息用
    View loadingAni;
    Button ch1Button;
    Button ch2Button;
    Button ch3Button;
    byte[] statusMsg; //裝置的狀態
    Boolean defaultUi = true; //判斷是否第一次進入控制畫面
    int failedCount = 0;//判斷是否為MaxScene沒有回應
    DeviceCommand deviceCommand = new DeviceCommand();
    ExecutorService exec = Executors.newCachedThreadPool();
    boolean isLongPressed = false;
    String sleepingMode;
    int sleepingModeCh;
    //呼吸燈動畫
    private final int BREATH_INTERVAL_TIME = 2000;
    private AlphaAnimation animationFadeIn;
    private AlphaAnimation animationFadeOut;
    boolean shouldRepeatAnimations = true;
    static boolean startAni = true;
    static boolean startAni2 = true;
    static boolean startAni3 = true;
    String sleepInfo;
    String sleepOrBright; //判斷該ch是漸亮還是漸暗
    String ch1SleepText = "Dimming Mode Setting"; //傳給Fragment應該設定的訊息
    String ch2SleepText = "Dimming Mode Setting";; //傳給Fragment應該設定的訊息
    String ch3SleepText = "Dimming Mode Setting";; //傳給Fragment應該設定的訊息
    //命名相關
    String subtitleText;
    Shortcut shortcut1 = new Shortcut();
    Shortcut shortcut2 = new Shortcut();
    Shortcut shortcut3 = new Shortcut();
    private List<Shortcut> sceneShortcutList; //當前頁面的捷徑資料
    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(Max3SettingActivity.this);
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
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(Max3SettingActivity.this).unregisterReceiver(myBroadcast);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(Max3SettingActivity.this);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_max3_setting);
        ch1Up = findViewById(R.id.ch1_up);
        ch1Down = findViewById(R.id.ch1_down);
        ch2Up = findViewById(R.id.ch2_up);
        ch2Down = findViewById(R.id.ch2_down);
        ch3Up = findViewById(R.id.ch3_up);
        ch3Down = findViewById(R.id.ch3_down);
        TextView subtitle = findViewById(R.id.subtitle);
        int colorRed = ContextCompat.getColor(this, R.color.red);
        int colorGray = ContextCompat.getColor(this, R.color.gray2);
        int colorBlue = ContextCompat.getColor(this, R.color.blue);
        //設定一般點擊
        setBtnClick(ch1Up, ch1Down, ch2Up, ch2Down, ch3Up, ch3Down, colorRed, colorGray, colorBlue);

        //設定長按動畫
        setMaxLiteOnAni(ch1Up, ch1Down, false);
        setMaxLiteOffAni(ch1Up, ch1Down, true);
        setMaxLiteOnAni(ch2Up, ch2Down, false);
        setMaxLiteOffAni(ch2Up, ch2Down, true);
        setMaxLiteOnAni(ch3Up, ch3Down, false);
        setMaxLiteOffAni(ch3Up, ch3Down, true);

        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        deviceSN = info.getByteArray("deviceSN");
        Log.d(TAG, "SN: " + UDP.bytesToHex(deviceSN));
        deviceName = info.getString("deviceName");
        deviceCh = info.getString("deviceCH","0");
        deviceChNum  = Integer.valueOf(deviceCh);
        subtitleText = info.getString("deviceSubtitle","");
        subtitle.setText(subtitleText);
        //捷徑初始化
        settingShortcut();

       /* //預設畫面
        BrightnessFragment ch1Frag = new BrightnessFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, ch1Frag);
        fragmentTransaction.commit();*/

        //取得動畫View
        loadingAni = findViewById(R.id.spin_kit);
        failedCount = 0;

        //呼吸燈動畫
        animationFadeIn = new AlphaAnimation(0.1f, 1.0f);
        animationFadeIn.setDuration(BREATH_INTERVAL_TIME);

        animationFadeOut = new AlphaAnimation(1.0f, 0.1f);
        animationFadeOut.setDuration(BREATH_INTERVAL_TIME);

        animationFadeIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (shouldRepeatAnimations) {
                    if (sleepingModeCh == 1) {
                        ch1Button.startAnimation(animationFadeOut);
                    }
                    if (sleepingModeCh == 2) {
                        ch2Button.startAnimation(animationFadeOut);
                    }
                    if (sleepingModeCh == 3) {
                        ch3Button.startAnimation(animationFadeOut);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

        });

        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (shouldRepeatAnimations) {
                    if (sleepingModeCh == 1) {
                        ch1Button.startAnimation(animationFadeIn);
                    }
                    if (sleepingModeCh == 2) {
                        ch2Button.startAnimation(animationFadeIn);
                    }
                    if (sleepingModeCh == 3) {
                        ch3Button.startAnimation(animationFadeIn);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

        });
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        ch1Button = (Button) findViewById(R.id.ch1_button);
        ch2Button = (Button) findViewById(R.id.ch2_button);
        ch3Button = (Button) findViewById(R.id.ch3_button);
        ch1Button.setSelected(true);
        //還未接收到裝置資料時按鈕不能使用
        ch1Button.setEnabled(false);
        ch2Button.setEnabled(false);
        ch3Button.setEnabled(false);
        ch1Up.setEnabled(false);
        ch2Up.setEnabled(false);
        ch3Up.setEnabled(false);
        ch1Down.setEnabled(false);
        ch2Down.setEnabled(false);
        ch3Down.setEnabled(false);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Max3SettingActivity.this, Device3Activity.class);
                intent.putExtra("id", 3);
                startActivity(intent);
                Animatoo.animateSlideRight(Max3SettingActivity.this);
            }
        });

        //控制畫面設定
        //fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //裝置名稱
        title.setText(deviceName);
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Max3SettingActivity.this,DeviceNameSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN",deviceSN);
                bundle.putString("deviceName",deviceName);
                bundle.putString("deviceCH",deviceCh);
                bundle.putString("deviceSubtitle",subtitleText);
                bundle.putInt("page",3);
                String type = String.format("%02X ", maxType);
                bundle.putString("type","18");
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(Max3SettingActivity.this);
                return false;
            }
        });

       /* new Thread(() -> {
            sceneShortcutList = ShortcutDataBase.getInstance(Max3SettingActivity.this).getShortcutDataDao(). findDataByhostSNandType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));

            //取得選擇當筆捷徑的資料
            if(sceneShortcutList.size()>0) {
                shortcut1 = sceneShortcutList.get(0);
                shortcut2 = sceneShortcutList.get(1);
                shortcut3 = sceneShortcutList.get(2);
            } else {
                shortcut1.setName("CH1");
                shortcut2.setName("CH2");
                shortcut3.setName("CH3");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   ch1Button.setText(shortcut1.getName());
                   ch2Button.setText(shortcut2.getName());
                   ch3Button.setText(shortcut3.getName());
                }
            });
        }).start();*/

        //ch按鍵邏輯設定
        ch1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ch1Button.setSelected(true);
                ch2Button.setSelected(false);
                ch3Button.setSelected(false);
                defineChOnScreen(ch1Type, 1);
               /* BrightnessFragment ch1Frag = new BrightnessFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, ch1Frag);
                fragmentTransaction.commit();*/

            }
        });
        ch1Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                settingRename(shortcut1,1);
                return false;
            }
        });
        ch2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ch1Button.setSelected(false);
                ch2Button.setSelected(true);
                ch3Button.setSelected(false);
                defineChOnScreen(ch2Type, 2);
               /* RelayFragment ch2Frag = new RelayFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, ch2Frag);
                fragmentTransaction.commit();*/

            }
        });
        ch2Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                settingRename(shortcut2,2);
                return false;
            }
        });
        ch3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ch1Button.setSelected(false);
                ch2Button.setSelected(false);
                ch3Button.setSelected(true);
                defineChOnScreen(ch3Type, 3);
               /* CurtainFragment ch3Frag = new CurtainFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, ch3Frag);
                fragmentTransaction.commit();*/
            }
        });
        ch3Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                settingRename(shortcut3,3);
                return false;
            }
        });
        //  listen RxJava event here
        RxBus.getInstance().listen().subscribe(getInputObserver());
        //連線相關
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        // connectUDP();
        //當一進到畫面時 先了解裝置目前的狀態
        statusCommand = deviceCommand.maxLite(3, deviceSN, 0, (byte) 0);
        maxLiteCommand(statusCommand);
        //當裝置沒有回應時  過3秒後重新發送指令
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (chInfo == null) {
                    maxLiteCommand(statusCommand);
                    failedCount++;
                }
            }
        }, 3000);

    }
    //按鍵動畫設定
    private void setChButtonOnAni(ImageView chUp){
        int colorFrom = ContextCompat.getColor(this, R.color.red);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni) {
                    chUp.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    //按鍵動畫設定
    private void setChButtonOffAni(ImageView chDown){
        int colorFrom = ContextCompat.getColor(this, R.color.blue);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni) {
                    chDown.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    //按鍵動畫設定
    private void setChButtonOnAni2(ImageView chUp){
        int colorFrom = ContextCompat.getColor(this, R.color.red);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni2) {
                    chUp.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    //按鍵動畫設定
    private void setChButtonOffAni2(ImageView chDown){
        int colorFrom = ContextCompat.getColor(this, R.color.blue);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni2) {
                    chDown.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    //按鍵動畫設定
    private void setChButtonOnAni3(ImageView chUp){
        int colorFrom = ContextCompat.getColor(this, R.color.red);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni2) {
                    chUp.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    //按鍵動畫設定
    private void setChButtonOffAni3(ImageView chDown){
        int colorFrom = ContextCompat.getColor(this, R.color.blue);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.setRepeatCount(ValueAnimator.INFINITE);
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if(startAni2) {
                    chDown.setColorFilter((int) animator.getAnimatedValue());
                }
            }

        });

        redToGraynimation.start();


    }
    public void  settingRename(Shortcut shortcut,int id){
       /* dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.scene_rename_popup,null);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(Max3SettingActivity.this); //final一個editText
        builder.setView(editText);
        editText.setText(shortcut.getName());
        builder.setTitle("Rename");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(()->{
                    String updateText;
                    updateText = editText.getText().toString();
                    shortcut.setName(updateText);
                    ShortcutDataBase.getInstance(Max3SettingActivity.this).getShortcutDataDao().updateMaxSceneOnlyName(shortcut.getName(), shortcut.getId());
                }).start();
                if(id==1){
                    ch1Button.setText(editText.getText().toString());
                }
                if(id==2){
                    ch2Button.setText(editText.getText().toString());
                }
                if(id==3){
                    ch3Button.setText(editText.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }
    private void setBtnClick(ImageView ch1Up, ImageView ch1Down, ImageView ch2Up, ImageView ch2Down, ImageView ch3Up, ImageView ch3Down, int colorRed, int colorGray, int colorBlue) {
        ch1Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String off = "0000";

                        ch1Up.setColorFilter(colorRed);
                        ch1Down.setColorFilter(colorGray);
                        defineBtnOnScreen(ch1Type, 1, off);
                        ch1Button.setSelected(true);
                        ch2Button.setSelected(false);
                        ch3Button.setSelected(false);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(1, false, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);

            }
        });
        ch2Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String off = "0000";
                        ch2Up.setColorFilter(colorRed);
                        ch2Down.setColorFilter(colorGray);
                        ch1Button.setSelected(false);
                        ch2Button.setSelected(true);
                        ch3Button.setSelected(false);
                        defineBtnOnScreen(ch2Type, 2, off);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(2, false, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);

            }
        });
        ch3Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String off = "0000";
                        ch3Up.setColorFilter(colorRed);
                        ch3Down.setColorFilter(colorGray);
                        defineBtnOnScreen(ch3Type, 3, off);
                        ch1Button.setSelected(false);
                        ch2Button.setSelected(false);
                        ch3Button.setSelected(true);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(3, false, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);

            }
        });
        ch1Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        String on = "03FF";
                        ch1Up.setColorFilter(colorGray);
                        ch1Down.setColorFilter(colorBlue);
                        ch1Button.setSelected(true);
                        ch2Button.setSelected(false);
                        ch3Button.setSelected(false);

                        defineBtnOnScreen(ch1Type, 1, on);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(1, true, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);


            }
        });
        ch2Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String on = "03FF";
                        ch2Up.setColorFilter(colorGray);
                        ch2Down.setColorFilter(colorBlue);
                        ch1Button.setSelected(false);
                        ch2Button.setSelected(true);
                        ch3Button.setSelected(false);
                        defineBtnOnScreen(ch2Type, 2, on);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(2, true, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);


            }
        });
        ch3Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String on = "03FF";
                        ch3Up.setColorFilter(colorGray);
                        ch3Down.setColorFilter(colorBlue);
                        ch1Button.setSelected(false);
                        ch2Button.setSelected(false);
                        ch3Button.setSelected(true);
                        defineBtnOnScreen(ch3Type, 3, on);
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(3, true, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        maxLiteCommand(sendCMD);
                    }
                }, 300);


            }
        });
    }

    //觀察fragment狀態並處理CH動畫
    private Observer<String> getInputObserver() {
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
                sleepingMode = s;
                Log.d(TAG, "sleepingMode: " + sleepingMode);
                if (s.length() > 12) {
                    sleepingMode = s.substring(0, 12);
                    sleepOrBright = s.substring(13);
                    sleepingModeCh = Integer.parseInt(s.substring(12,13));
                } else {
                    sleepingMode = s.substring(0, 4);
                    sleepingModeCh = Integer.parseInt(s.substring(4));
                }

                if (sleepingMode.equals("sleepingMode")) {
                  /*  if (sleepingModeCh == 1) {
                        shouldRepeatAnimations = true;
                        ch1Button.startAnimation(animationFadeOut);
                    }
                    if (sleepingModeCh == 2) {
                        shouldRepeatAnimations = true;
                        ch2Button.startAnimation(animationFadeOut);
                    }
                    if (sleepingModeCh == 3) {
                        shouldRepeatAnimations = true;
                        ch3Button.startAnimation(animationFadeOut);
                    }*/

                     if (sleepingModeCh == 1) {
                        startAni = true;
                       /* shouldRepeatAnimations = true;
                        ch1Button.startAnimation(animationFadeOut);*/
                        if(sleepOrBright.equals("0")){
                            setChButtonOffAni(ch1Down);
                            setChButtonOnAni(ch1Up);
                        }
                        if(sleepOrBright.equals("1")){
                            setChButtonOnAni(ch1Up);
                            setChButtonOffAni(ch1Down);
                        }
                    }
                    if (sleepingModeCh == 2) {
                        startAni2 = true;
                        /*shouldRepeatAnimations = true;
                        ch2Button.startAnimation(animationFadeOut);*/
                        if(sleepOrBright.equals("0")){
                            setChButtonOffAni2(ch2Down);
                            setChButtonOnAni2(ch2Up);
                        }
                        if(sleepOrBright.equals("1")){
                            setChButtonOnAni2(ch2Up);
                            setChButtonOffAni2(ch2Down);
                        }
                    }
                    if (sleepingModeCh == 3) {
                        startAni3 = true;
                        /*shouldRepeatAnimations = true;
                        ch2Button.startAnimation(animationFadeOut);*/
                        if(sleepOrBright.equals("0")){
                            setChButtonOffAni3(ch3Down);
                            setChButtonOnAni3(ch3Up);
                        }
                        if(sleepOrBright.equals("1")){
                            setChButtonOnAni3(ch3Up);
                            setChButtonOffAni3(ch3Down);
                        }
                    }
                }

                if (sleepingMode.equals("stop")) {
                  /*  if (sleepingModeCh == 1) {
                        shouldRepeatAnimations = false;
                        ch1Button.clearAnimation();
                    }
                    if (sleepingModeCh == 2) {
                        shouldRepeatAnimations = false;
                        ch2Button.clearAnimation();
                    }
                    if (sleepingModeCh == 3) {
                        shouldRepeatAnimations = false;
                        ch3Button.clearAnimation();
                    }*/



                    if (sleepingModeCh == 1) {
                        startAni = false;
                        /*shouldRepeatAnimations = false;
                        ch1Button.clearAnimation()*/;
                        ch1Down.clearAnimation();
                        ch1Up.clearAnimation();

                    }
                    if (sleepingModeCh == 2) {
                        startAni2 = false;
                        /*shouldRepeatAnimations = false;
                        ch2Button.clearAnimation();*/
                        ch2Down.clearAnimation();
                        ch2Up.clearAnimation();

                    }
                    if (sleepingModeCh == 3) {
                        startAni3 = false;
                        /*shouldRepeatAnimations = false;
                        ch2Button.clearAnimation();*/
                        ch3Down.clearAnimation();
                        ch3Up.clearAnimation();

                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    //長按動畫
    @SuppressLint("ClickableViewAccessibility")
    private void setMaxLiteOnAni(ImageView ch1Up, ImageView ch1Down, boolean lightOn) {
        int colorFrom = ContextCompat.getColor(this, R.color.red);
        int colorTo = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator redToGraynimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        redToGraynimation.setDuration(3000); // milliseconds
        redToGraynimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ch1Up.setColorFilter((int) animator.getAnimatedValue());
            }

        });
        int colorFrom1 = ContextCompat.getColor(this, R.color.gray2);
        int colorTo1 = ContextCompat.getColor(this, R.color.blue);
        @SuppressLint("RestrictedApi") ValueAnimator grayToBlueAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom1, colorTo1);
        grayToBlueAnimation.setDuration(3000); // milliseconds
        grayToBlueAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ch1Down.setColorFilter((int) animator.getAnimatedValue());
            }

        });

        ch1Up.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                redToGraynimation.start();
                grayToBlueAnimation.start();
                isLongPressed = true;

                return true;
            }
        });


        ch1Up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                // We're only interested in when the button is released.
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // We're only interested in anything if our  button is currently pressed.
                    if (isLongPressed) {
                        // Do something when the button is released.
                        grayToBlueAnimation.cancel();
                        redToGraynimation.cancel();
                        isLongPressed = false;
                    }
                }


                return true; //return true避免重複觸發onclick
            }

        });
    }

    //長按動畫
    @SuppressLint("ClickableViewAccessibility")
    private void setMaxLiteOffAni(ImageView ch1Up, ImageView ch1Down, boolean lightOn) {
        int colorFrom = ContextCompat.getColor(this, R.color.gray2);
        int colorTo = ContextCompat.getColor(this, R.color.red);
        @SuppressLint("RestrictedApi") ValueAnimator grayToRednimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        grayToRednimation.setDuration(5000); // milliseconds
        grayToRednimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ch1Up.setColorFilter((int) animator.getAnimatedValue());
            }

        });
        int colorFrom1 = ContextCompat.getColor(this, R.color.blue);
        int colorTo1 = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator blueToGrayAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom1, colorTo1);
        blueToGrayAnimation.setDuration(5000); // milliseconds
        blueToGrayAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ch1Down.setColorFilter((int) animator.getAnimatedValue());
            }

        });

        ch1Down.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                grayToRednimation.start();
                blueToGrayAnimation.start();
                isLongPressed = true;


                return true;
            }
        });


        ch1Down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (lightOn) {
                    v.onTouchEvent(event);
                    // We're only interested in when the button is released.
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // We're only interested in anything if our speak button is currently pressed.
                        if (isLongPressed) {
                            // Do something when the button is released.
                            blueToGrayAnimation.cancel();
                            grayToRednimation.cancel();
                            isLongPressed = false;
                        }
                    }


                }
                return true; //return true避免重複觸發onclick
            }

        });
    }
    private void settingShortcut() {
        final Thread th2 =   new Thread(() -> {
            sceneShortcutList = ShortcutDataBase.getInstance(Max3SettingActivity.this).getShortcutDataDao(). findDataByhostSNandType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));

            //取得選擇當筆捷徑的資料
            if(sceneShortcutList.size()>0) {
                shortcut1 = sceneShortcutList.get(0);
                shortcut2 = sceneShortcutList.get(1);
                shortcut3 = sceneShortcutList.get(2);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   ch1Button.setText(shortcut1.getName());
                   ch2Button.setText(shortcut2.getName());
                   ch3Button.setText(shortcut3.getName());
                }
            });
        });

       final Thread th1 = new Thread(() -> {
            shortcutData = ShortcutDataBase.getInstance(Max3SettingActivity.this).getShortcutDataDao().findDeviceSNandDeviceType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));
            if(shortcutData.size()==0) {
                for (int i = 1; i < 4; i++) {
                    Shortcut shortcut = new Shortcut();
                    shortcut.setName("CH" + i);
                    shortcut.setDeciveCH("CH" + i);
                    shortcut.setHostDeviceSN(UDP.byteArrayToHexStr(deviceSN));
                    shortcut.setDeviceType(String.format("%02X ", maxType));
                    shortcut.setHostDeviceName(deviceName);
                    ShortcutDataBase.getInstance(Max3SettingActivity.this).getShortcutDataDao().insertData(shortcut);
                    if(i==1){
                        ch1Button.setText(shortcut.getName());
                        shortcut1 = shortcut;
                    }
                    if(i==2){
                        ch2Button.setText(shortcut.getName());
                        shortcut2 = shortcut;
                    }
                    if(i==3){
                        ch3Button.setText(shortcut.getName());
                        shortcut3 = shortcut;
                    }
                }
            }
            th2.start();
        });
        th1.start();

    }
    //發送指令
    private void maxLiteCommand(byte msgCRC[]) {
        statusMsg = msgCRC;
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

    //定義每個CH的種類
    public String defineChType(String chType) {
        switch (chType) {
            case "00":
                chType = "dim";
                break;
            case "01":
                chType = "relay";
                break;
            case "10":
                chType = "cct";
                break;
            case "11":
                chType = "curtain";
                break;

        }
        return chType;
    }

    //根據每個CH的種類來顯示相對應的畫面
    public void defineChOnScreen(String chType, int chNum) {
        Bundle bundle = new Bundle();
        byte maxType = 0x18; //設定控制的裝置類型 18為3切
        switch (chType) {
            case "dim":
                bundle.putInt("chNum", chNum);
                Log.d(TAG, "CH" + chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putString("ch3Brightness", ch3Brightness);
                bundle.putString("deviceName", deviceName);
                bundle.putByte("maxType", maxType);
                BrightnessFragment chFrag = new BrightnessFragment();
                chFrag.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "relay":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putString("ch3Brightness", ch3Brightness);
                bundle.putString("deviceName", deviceName);
                bundle.putByte("maxType", maxType);
                RelayFragment chFrag1 = new RelayFragment();
                chFrag1.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag1);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "cct":
                bundle.putInt("chNum", chNum);

                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putString("ch1Temperature", ch1Temperature);
                bundle.putString("ch2Temperature", ch2Temperature);
                bundle.putString("ch3Temperature", ch3Temperature);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putString("ch3Brightness", ch3Brightness);
                bundle.putString("deviceName", deviceName);
                bundle.putByte("maxType", maxType);
                ColorTemperatureFragment chFrag2 = new ColorTemperatureFragment();
                chFrag2.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag2);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "curtain":
                Log.d(TAG, "CH: "+chNum);

                CurtainNoDegreeFragment chFrag3 = new CurtainNoDegreeFragment();
                CurtainFragment chFrag4 = new CurtainFragment();
                BlindsFragment chFrag5 = new BlindsFragment();
                if (ch1CurtainType != null || ch2CurtainType != null || ch3CurtainType != null) {
                    if(chNum==1){
                        if(ch1CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch1CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch1CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    }
                    if(chNum==2){
                        if(ch2CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch2CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch2CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    }
                    if(chNum==3){
                        if(ch3CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch3CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if(ch3CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    }
                    /*     if (ch1CurtainType.equals("00") || ch2CurtainType.equals("00") || ch3CurtainType.equals("00")) {
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                        chFrag3.setArguments(bundle);
                        fragmentTransaction.commit();
                    }
                    if (ch1CurtainType.equals("11") || ch2CurtainType.equals("11") || ch3CurtainType.equals("11")) {
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                        chFrag4.setArguments(bundle);
                        fragmentTransaction.commit();
                    }*/
                }
                break;

        }

    }


    //根據按鈕來顯示CH對應的畫面
    public void defineBtnOnScreen(String chType, int chNum, String light) {
        Bundle bundle = new Bundle();
        byte maxType = 0x18; //設定控制的裝置類型 18為3切
        switch (chType) {
            case "dim":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("ch1Brightness", light);
                bundle.putString("ch2Brightness", light);
                bundle.putString("ch3Brightness", light);
                bundle.putString("deviceName", deviceName);
                BrightnessFragment chFrag = new BrightnessFragment();
                chFrag.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag);
                fragmentTransaction.commit();
                break;
            case "relay":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("ch1Brightness", light);
                bundle.putString("ch2Brightness", light);
                bundle.putString("ch3Brightness", light);
                bundle.putString("deviceName", deviceName);
                RelayFragment chFrag1 = new RelayFragment();
                chFrag1.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag1);
                fragmentTransaction.commit();
                break;
            case "cct":
                bundle.putInt("chNum", chNum);
                Log.d(TAG, "CH" + chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("ch1Brightness", light);
                bundle.putString("ch2Brightness", light);
                bundle.putString("ch3Brightness", light);
                bundle.putString("deviceName", deviceName);
                ColorTemperatureFragment chFrag2 = new ColorTemperatureFragment();
                chFrag2.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag2);
                fragmentTransaction.commit();
                break;
            case "curtain":
                Log.d(TAG, "ch: "+chNum);
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("deviceName", deviceName);
                CurtainNoDegreeFragment chFrag3 = new CurtainNoDegreeFragment();
                CurtainFragment chFrag4 = new CurtainFragment();
                BlindsFragment chFrag5 = new BlindsFragment();
                if (ch1CurtainType != null || ch2CurtainType != null || ch3CurtainType != null) {
                    if(chNum==1){
                        if(ch1CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commit();
                        }
                        if(ch1CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commit();
                        }
                        if(ch1CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);

                            fragmentTransaction.commit();
                        }
                    }
                    if(chNum==2){
                        if(ch2CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commit();
                        }
                        if(ch2CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commit();
                        }
                        if(ch2CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);

                            fragmentTransaction.commit();
                        }
                    }
                    if(chNum==3){
                        if(ch3CurtainType.equals("00")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commit();
                        }
                        if(ch3CurtainType.equals("11")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commit();
                        }
                        if(ch3CurtainType.equals("10")){
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","18");
                            bundle.putInt("page",3);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commit();
                        }
                    }
                    /*     if (ch1CurtainType.equals("00") || ch2CurtainType.equals("00") || ch3CurtainType.equals("00")) {
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                        chFrag3.setArguments(bundle);
                        fragmentTransaction.commit();
                    }
                    if (ch1CurtainType.equals("11") || ch2CurtainType.equals("11") || ch3CurtainType.equals("11")) {
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                        chFrag4.setArguments(bundle);
                        fragmentTransaction.commit();
                    }*/
                }
                break;


        }

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
                    String resultSN = "";
                    int length = bundle.getInt(UDP.RECEIVE_DATALENGTH);//資料實際的長度 這樣才可以拿出多餘的0
                    //將多餘的0濾除
                    byte[] resultBytes = new byte[length];
                    for (int i = 0; i < length; i++) {
                        resultBytes[i] = bytes[i];
                    }
                    //Log.d(TAG, "bytes: "+resultBytes);
                    String hex = UDP.byteArrayToHexStr(resultBytes);
                    Log.d(TAG, "onMax3Receive: " + hex);
                    // Log.d(TAG, "onMax3Receive: " + hex.length());
                    if (Arrays.equals(resultBytes, statusMsg)&& chInfo==null) {
                        if (failedCount ==1  && showDialog && chInfo==null) {
                            new AlertDialog.Builder(Max3SettingActivity.this)
                                    .setTitle("裝置沒有回應")
                                    .setMessage("裝置沒有回應，請確認手機是否與裝置在同一個WiFi")
                                    .setPositiveButton("OK", null)
                                    .show();

                            loadingAni.setVisibility(View.GONE);
                            showDialog = false;
                            Log.d(TAG, "MaxScene沒有回應");
                        }
                    }
                    if (!hex.contains("1806") && resultBytes.length == 25||resultBytes.length==50) {
                        resultSN = hex.substring(32, 44);//回傳指令的裝置SN
                        //   Log.d(TAG, "長度: "+resultBytes.length);
                        // Log.d(TAG, "裝置SN"+resultSN+"deviceSN:"+UDP.bytesToHex(deviceSN));
                    }
                    if (resultSN.equals(UDP.bytesToHex(deviceSN))) {
                        setChInfo(hex);
                        //睡眠狀態判斷
                        sleepInfo = hex.substring(12,14);
                        sleepInfo = UDP.hexToBin(sleepInfo);
                        String ch1Sleep = sleepInfo.substring(2,4);
                        String ch2Sleep = sleepInfo.substring(4,6);
                        String ch3Sleep = sleepInfo.substring(6);
                        if(ch1Sleep.equals("01")||ch1Sleep.equals("10")){
                            startAni = true;
                            setChButtonOnAni(ch1Up);
                            setChButtonOffAni(ch1Down);
                        }
                        if(ch1Sleep.equals("00")){
                            startAni = false;
                            ch1Up.clearAnimation();
                            ch1Down.clearAnimation();
                        }
                        if(ch2Sleep.equals("00")){
                            startAni2 = false;
                            ch2Up.clearAnimation();
                            ch2Down.clearAnimation();
                        }
                        if(ch2Sleep.equals("01")||ch2Sleep.equals("10")){
                            startAni2 = true;
                            setChButtonOnAni2(ch2Up);
                            setChButtonOffAni2(ch2Down);
                        }
                        if(ch3Sleep.equals("00")){
                            startAni3 = false;
                            ch3Up.clearAnimation();
                            ch3Down.clearAnimation();
                        }
                        if(ch3Sleep.equals("01")||ch3Sleep.equals("10")){
                            startAni3 = true;
                            setChButtonOnAni3(ch3Up);
                            setChButtonOffAni3(ch3Down);
                        }

                       /* if(ch1Sleep.equals("01")){
                            startAni = true;
                            startAni2 = false;
                            startAni3 = false;
                            setChButtonOnAni(ch1Up);
                        }
                        if(ch1Sleep.equals("10")){
                            startAni = true;
                            startAni2 = false;
                            startAni3 = false;
                            setChButtonOffAni(ch1Down);
                        }
                        if(ch1Sleep.equals("00")){
                            startAni = false;

                            ch1Up.clearAnimation();
                            ch1Down.clearAnimation();
                        }
                        if(ch2Sleep.equals("01")){
                            startAni = false;
                            startAni2 = true;
                            startAni3 = false;
                            setChButtonOnAni2(ch2Up);
                        }
                        if(ch2Sleep.equals("10")){
                            startAni = false;
                            startAni2 = true;
                            startAni3 = false;
                            setChButtonOffAni2(ch2Down);
                        }
                        if(ch2Sleep.equals("00")){

                            startAni2 = false;

                            ch2Up.clearAnimation();
                            ch2Down.clearAnimation();
                        }
                        if(ch3Sleep.equals("01")){
                            startAni = false;
                            startAni2 = false;
                            startAni3 = true;
                            setChButtonOnAni3(ch3Up);
                        }
                        if(ch3Sleep.equals("10")){
                            startAni = false;
                            startAni2 = false;
                            startAni3 = true;
                            setChButtonOffAni3(ch3Down);
                        }
                        if(ch3Sleep.equals("00")){

                            startAni3 = false;
                            ch3Up.clearAnimation();
                            ch3Down.clearAnimation();
                        }*/
                        if (defaultUi) {
                            if(deviceChNum==0) {
                                defineChOnScreen(ch1Type, 1);
                                loadingAni.setVisibility(View.GONE);
                                ch1Button.setEnabled(true);
                                ch2Button.setEnabled(true);
                                ch3Button.setEnabled(true);
                                ch1Up.setEnabled(true);
                                ch2Up.setEnabled(true);
                                ch3Up.setEnabled(true);
                                ch1Down.setEnabled(true);
                                ch2Down.setEnabled(true);
                                ch3Down.setEnabled(true);
                                defaultUi = false;
                                //從捷徑進來的邏輯
                            } else {
                                if(deviceChNum==1){
                                    defineChOnScreen(ch1Type, deviceChNum);
                                    ch1Button.setSelected(true);
                                    ch2Button.setSelected(false);
                                    ch3Button.setSelected(false);
                                }
                                if(deviceChNum==2){
                                    defineChOnScreen(ch2Type, deviceChNum);
                                    ch1Button.setSelected(false);
                                    ch2Button.setSelected(true);
                                    ch3Button.setSelected(false);
                                }
                                if(deviceChNum==3){
                                    defineChOnScreen(ch3Type, deviceChNum);
                                    ch1Button.setSelected(false);
                                    ch2Button.setSelected(false);
                                    ch3Button.setSelected(true);
                                }
                                loadingAni.setVisibility(View.GONE);
                                ch1Button.setEnabled(true);
                                ch2Button.setEnabled(true);
                                ch3Button.setEnabled(true);
                                ch1Up.setEnabled(true);
                                ch2Up.setEnabled(true);
                                ch3Up.setEnabled(true);
                                ch1Down.setEnabled(true);
                                ch2Down.setEnabled(true);
                                ch3Down.setEnabled(true);
                                defaultUi = false;
                            }
                        }
                        // Log.d(TAG, "ch1: " + ch1Type + " ch2: " + ch2Type + " ch3:" + ch3Type);
                    }

                    break;

            }
        }

        private void setChInfo(String hex) {
            chInfo = hex.substring(14, 16);//指令第7個Byte(轉成字串第14 15個)
            Log.d(TAG, "setChInfo: "+chInfo);
            chInfo = UDP.hexToBin(chInfo); //轉成二進位
            Log.d(TAG, "setChInfo: "+chInfo);
            String curtainInfo = hex.substring(16, 18);
            Log.d(TAG, "curtainInfo: "+curtainInfo);
            curtainInfo = UDP.hexToBin(curtainInfo);
           // Log.d(TAG, "curtain: "+curtainInfo);
            ch1CurtainType = curtainInfo.substring(2, 4);
            ch2CurtainType = curtainInfo.substring(4, 6);
            ch3CurtainType = curtainInfo.substring(6);
              Log.d(TAG, "ch3CurtainType:" + ch3CurtainType);
            ch1Type = chInfo.substring(2, 4);
            ch1Type = defineChType(ch1Type);
            ch1Brightness = hex.substring(8, 12);
            ch1Temperature = hex.substring(22, 24);
            ch2Type = chInfo.substring(4, 6);
            ch2Type = defineChType(ch2Type);
            ch2Brightness = hex.substring(4, 8);
            ch2Temperature = hex.substring(20, 22);
            ch3Type = chInfo.substring(6);
            Log.d(TAG, "ch3Type: "+ch3Type);
            ch3Type = defineChType(ch3Type);
            ch3Brightness = hex.substring(0, 4);
            ch3Temperature = hex.substring(18, 20);
            settingBtnStatus();
        }

        //顯示上方按鈕的UI
        private void settingBtnStatus() {
            int colorRed = ContextCompat.getColor(Max3SettingActivity.this, R.color.red);
            int colorGray = ContextCompat.getColor(Max3SettingActivity.this, R.color.gray2);
            int colorBlue = ContextCompat.getColor(Max3SettingActivity.this, R.color.blue);
            int num1 = Integer.parseInt(ch1Brightness, 16);
            float percent1 = (float) num1 / 1023;
            int num2 = Integer.parseInt(ch2Brightness, 16);
            float percent2 = (float) num2 / 1023;
            int num3 = Integer.parseInt(ch3Brightness, 16);
            float percent3 = (float) num3 / 1023;
           // Log.d(TAG, "p1: " + percent1 + " p2:" + percent2 + " p3:" + percent3);
            if (percent1 == 1.0f || percent1 >= 0.99f) {
                ch1Up.setColorFilter(colorGray);
                ch1Down.setColorFilter(colorBlue);
                ch1Up.setAlpha(1.0f);
                ch1Down.setAlpha(1.0f);
            }
            if (percent2 == 1.0f || percent2 >= 0.99f) {
                ch2Up.setColorFilter(colorGray);
                ch2Down.setColorFilter(colorBlue);
                ch2Up.setAlpha(1.0f);
                ch2Down.setAlpha(1.0f);
            }
            if (percent3 == 1.0f || percent3 >= 0.99f) {
                ch3Up.setColorFilter(colorGray);
                ch3Down.setColorFilter(colorBlue);
                ch3Up.setAlpha(1.0f);
                ch3Down.setAlpha(1.0f);
            }
            if (percent1 == 0.0f) {
                ch1Up.setColorFilter(colorRed);
                ch1Down.setColorFilter(colorGray);
                ch1Up.setAlpha(1.0f);
                ch1Down.setAlpha(1.0f);
            }
            if (percent2 == 0.0f) {
                ch2Up.setColorFilter(colorRed);
                ch2Down.setColorFilter(colorGray);
                ch2Up.setAlpha(1.0f);
                ch2Down.setAlpha(1.0f);
            }
            if (percent3 == 0.0f) {
                ch3Up.setColorFilter(colorRed);
                ch3Down.setColorFilter(colorGray);
                ch3Up.setAlpha(1.0f);
                ch3Down.setAlpha(1.0f);
            } else if (percent1 != 1.0f && percent1 != 0.0f) {
                float alpha = 1 - percent1;
                if (alpha < 0.3f) {
                    alpha = 0.3f;
                }
                if (percent1 < 0.3f) {
                    percent1 = 0.3f;
                }
                ch1Up.setColorFilter(colorRed);
                ch1Down.setColorFilter(colorBlue);
                ch1Up.setAlpha(alpha);
                ch1Down.setAlpha(percent1);
            } else if (percent2 != 1.0f && percent2 != 0.0f) {
                float alpha = 1 - percent2;
                if (alpha < 0.3f) {
                    alpha = 0.3f;
                }
                if (percent2 < 0.3f) {
                    percent2 = 0.3f;
                }
                ch2Up.setColorFilter(colorRed);
                ch2Down.setColorFilter(colorBlue);
                ch2Up.setAlpha(alpha);
                ch2Down.setAlpha(percent2);
            } else if (percent3 != 1.0f && percent3 != 0.0f) {
                float alpha = 1 - percent3;
                if (alpha < 0.3f) {
                    alpha = 0.3f;
                }
                if (percent3 < 0.3f) {
                    percent3 = 0.3f;
                }
                ch3Up.setColorFilter(colorRed);
                ch3Down.setColorFilter(colorBlue);
                ch3Up.setAlpha(alpha);
                ch3Down.setAlpha(percent3);
            }
        }
    }
}

