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
import android.os.Handler;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class Max2SettingActivity extends AppCompatActivity {
    private static final String TAG = "max2setting";
    FragmentTransaction fragmentTransaction;
    Max2SettingActivity.MyBroadcast myBroadcast = new Max2SettingActivity.MyBroadcast();
    byte[] deviceSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    String deviceCh;
    List<Shortcut>shortcutData;
    int deviceChNum=1;
    String chInfo; //用來判斷裝置的種類
    byte[] statusCommand; //裝置目前的狀態
    byte maxType = 0x16; //設定控制的裝置類型 16為2切
    String ch1Type;
    String ch2Type;
    static boolean startAni = true;
    static boolean startAni2 = true;
    static ImageView ch1Up;
    static ImageView ch1Down;
    static ImageView ch2Up;
    static ImageView ch2Down;
    String ch1Brightness;
    String ch1Temperature;
    String ch2Brightness;
    String ch2Temperature;
    String ch1CurtainType;
    String ch2CurtainType;
    View loadingAni;
    Button ch1Button;
    Button ch2Button;
    String sleepInfo;
    byte[] statusMsg;
    boolean isLongPressed = false;
    Observable observable;
    boolean showDialog = true;//顯示錯誤訊息用
    Boolean defaultUi = true; //判斷是否第一次進入控制畫面
    int failedCount = 0;//判斷是否為MaxScene沒有回應
    DeviceCommand deviceCommand = new DeviceCommand();
    ExecutorService exec = Executors.newCachedThreadPool();
    String sleepingMode;
    String sleepOrBright; //判斷該ch是漸亮還是漸暗
    int sleepingModeCh;
    //呼吸燈動畫
    private final int BREATH_INTERVAL_TIME = 2000;
    private AlphaAnimation animationFadeIn;
    private AlphaAnimation animationFadeOut;
    boolean shouldRepeatAnimations = true;
    //命名相關
    String subtitleText;
    Shortcut shortcut1 = new Shortcut();
    Shortcut shortcut2 = new Shortcut();
    private List<Shortcut> sceneShortcutList; //當前頁面的捷徑資料
    /*@Override
    public void onDataPass(String data) {
        Log.d("LOG","hello " + data);
    }*/
    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(Max2SettingActivity.this);
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
        LocalBroadcastManager.getInstance(Max2SettingActivity.this).unregisterReceiver(myBroadcast);
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(Max2SettingActivity.this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_max2_setting);
        ch1Up = findViewById(R.id.ch1_up);
        ch1Down = findViewById(R.id.ch1_down);
        ch2Up = findViewById(R.id.ch2_up);
        ch2Down = findViewById(R.id.ch2_down);

        int colorRed = ContextCompat.getColor(this, R.color.red);
        int colorGray = ContextCompat.getColor(this, R.color.gray2);
        int colorBlue = ContextCompat.getColor(this, R.color.blue);
        //設定一般點擊
        setBtnClick(ch1Up, ch1Down, ch2Up, ch2Down, colorRed, colorGray, colorBlue);

        //設定長按動畫
        /*setMaxLiteOnAni(ch1Up, ch1Down, false);
        setMaxLiteOffAni(ch1Up, ch1Down, true);
        setMaxLiteOnAni(ch2Up, ch2Down, false);
        setMaxLiteOffAni(ch2Up, ch2Down, true);
*/
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


        //取得動畫View
        loadingAni = findViewById(R.id.spin_kit);

        failedCount = 0;
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        // SeekBar seekBar = findViewById(R.id.seekBar);
        ch1Button = (Button) findViewById(R.id.ch1_button);
        ch2Button = (Button) findViewById(R.id.ch2_button);
        ch1Button.setEnabled(false);
        ch2Button.setEnabled(false);
        ch1Up.setEnabled(false);
        ch2Up.setEnabled(false);
        ch2Down.setEnabled(false);
        ch2Down.setEnabled(false);

        TextView subtitle = findViewById(R.id.subtitle);

        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        deviceSN = info.getByteArray("deviceSN");
        deviceName = info.getString("deviceName");
        deviceCh = info.getString("deviceCH","0");
        subtitleText = info.getString("deviceSubtitle","");
        subtitle.setText(subtitleText);
        deviceChNum  = Integer.valueOf(deviceCh);
        //捷徑初始化
        settingShortcut();
        title.setText(deviceName);
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Max2SettingActivity.this,DeviceNameSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN",deviceSN);
                bundle.putString("deviceName",deviceName);
                bundle.putString("deviceCH",deviceCh);
                bundle.putString("deviceSubtitle",subtitleText);
                bundle.putInt("page",2);
                String type = String.format("%02X ", maxType);
                bundle.putString("type","16");
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(Max2SettingActivity.this);
                return false;
            }
        });
        ch1Button.setSelected(true);

        //預設頁面
     /*   Handler handler = new Handler();
        handler.postDelayed(new Runnable(){

            @Override
            public void run() {
                defineChOnScreen(ch1Type);
            }}, 3000);*/
       /* ColorTemperatureFragment ch1Frag = new ColorTemperatureFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, ch1Frag);
        fragmentTransaction.commit();*/

        //返回按鍵
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Max2SettingActivity.this, Device3Activity.class);
                intent.putExtra("id", 2);
                startActivity(intent);
                Animatoo.animateSlideRight(Max2SettingActivity.this);
            }
        });
       /* new Thread(() -> {
            sceneShortcutList = ShortcutDataBase.getInstance(Max2SettingActivity.this).getShortcutDataDao(). findDataByhostSNandType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));

            //取得選擇當筆捷徑的資料
            if(sceneShortcutList.size()>0) {
                shortcut1 = sceneShortcutList.get(0);
                shortcut2 = sceneShortcutList.get(1);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ch1Button.setText(shortcut1.getName());
                    ch2Button.setText(shortcut2.getName());
                }
            });
        }).start();*/

        //ch按鍵邏輯設定
        ch1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ch1Button.setSelected(true);
                ch2Button.setSelected(false);
                defineChOnScreen(ch1Type, 1);
               /* ColorTemperatureFragment ch1Frag = new ColorTemperatureFragment();
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
                defineChOnScreen(ch2Type, 2);
               /* CurtainNoDegreeFragment ch2Frag = new CurtainNoDegreeFragment();
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
        //  listen RxJava event here
        RxBus.getInstance().listen().subscribe(getInputObserver());
        //連線相關
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        // connectUDP();
        //當一進到畫面時 先了解裝置目前的狀態
        statusCommand = deviceCommand.maxLite(2, deviceSN, 0, (byte) 0);
       // Log.d(TAG, "狀態指令: " + byteArrayToHexStr(statusCommand));
        statusMsg = statusCommand;
        maxLiteCommand(statusCommand);
        //當裝置沒有回應時  過3秒後重新發送指令
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (chInfo == null) {
                    failedCount++;
                    maxLiteCommand(statusCommand);
                }
            }
        }, 3000);


    }
    public void  settingRename(Shortcut shortcut,int id){
       /* dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.scene_rename_popup,null);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(Max2SettingActivity.this); //final一個editText
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
                    ShortcutDataBase.getInstance(Max2SettingActivity.this).getShortcutDataDao().updateMaxSceneOnlyName(shortcut.getName(), shortcut.getId());
                }).start();
                if(id==1){
                    ch1Button.setText(editText.getText().toString());
                }
                if(id==2){
                    ch2Button.setText(editText.getText().toString());
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
        grayToRednimation.setDuration(3000); // milliseconds
        grayToRednimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ch1Up.setColorFilter((int) animator.getAnimatedValue());
            }

        });
        int colorFrom1 = ContextCompat.getColor(this, R.color.blue);
        int colorTo1 = ContextCompat.getColor(this, R.color.gray2);
        @SuppressLint("RestrictedApi") ValueAnimator blueToGrayAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom1, colorTo1);
        blueToGrayAnimation.setDuration(3000); // milliseconds
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

       final Thread th2 =  new Thread(() -> {
            sceneShortcutList = ShortcutDataBase.getInstance(Max2SettingActivity.this).getShortcutDataDao(). findDataByhostSNandType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));

            //取得選擇當筆捷徑的資料
            if(sceneShortcutList.size()>0) {
                shortcut1 = sceneShortcutList.get(0);
                shortcut2 = sceneShortcutList.get(1);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ch1Button.setText(shortcut1.getName());
                    ch2Button.setText(shortcut2.getName());
                }
            });
        });
        final Thread th1 =  new Thread(() -> {
            shortcutData = ShortcutDataBase.getInstance(Max2SettingActivity.this).getShortcutDataDao().findDeviceSNandDeviceType(UDP.byteArrayToHexStr(deviceSN),String.format("%02X ", maxType));
            if(shortcutData.size()==0) {
                for (int i = 1; i < 3; i++) {
                    Shortcut shortcut = new Shortcut();
                    shortcut.setName("CH" + i);
                    shortcut.setDeciveCH("CH" + i);
                    shortcut.setHostDeviceSN(UDP.byteArrayToHexStr(deviceSN));
                    shortcut.setDeviceType(String.format("%02X ", maxType));
                    shortcut.setHostDeviceName(deviceName);
                    ShortcutDataBase.getInstance(Max2SettingActivity.this).getShortcutDataDao().insertData(shortcut);
                    if(i==1){
                        ch1Button.setText(shortcut.getName());
                        shortcut1 = shortcut;
                    }
                    if(i==2){
                        ch2Button.setText(shortcut.getName());
                        shortcut2 = shortcut;
                    }
                }
            }
            th2.start();
        });
       th1.start();

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
                 Log.d(TAG, "sleepingMode: "+sleepingMode);
                if(s.length()>12) {
                    sleepingMode = s.substring(0, 12);
                    sleepOrBright = s.substring(13);
                    sleepingModeCh = Integer.parseInt(s.substring(12,13));
                } else {
                    sleepingMode = s.substring(0, 4);
                    sleepingModeCh = Integer.parseInt(s.substring(4));
                }

                if (sleepingMode.equals("sleepingMode")) {

                    if (sleepingModeCh == 1) {
                        startAni = true;
                       /* shouldRepeatAnimations = true;
                        ch1Button.startAnimation(animationFadeOut);*/
                        if(sleepOrBright.equals("0")){
                            setChButtonOnAni(ch1Up);
                            setChButtonOffAni(ch1Down);
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
                }
                if(sleepingMode.equals("stop")){


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

    private void setBtnClick(ImageView ch1Up, ImageView ch1Down, ImageView ch2Up, ImageView ch2Down, int colorRed, int colorGray, int colorBlue) {
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
                        byte[] chData;
                        byte[] sendCMD;
                        chData = deviceCommand.settingMaxLiteBtnCMD(1, false, maxType);
                        sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                        Log.d(TAG, "亮度指令: " + UDP.byteArrayToHexStr(sendCMD));
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
        switch (chType) {
            case "dim":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putByte("maxType", maxType);
                bundle.putString("deviceName", deviceName);
                BrightnessFragment chFrag = new BrightnessFragment();
                chFrag.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "relay":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putString("deviceName", deviceName);
                RelayFragment chFrag1 = new RelayFragment();
                chFrag1.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag1);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "cct":
                bundle.putInt("chNum", chNum);
                Log.d(TAG, "CH" + chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putString("ch1Temperature", ch1Temperature);
                bundle.putString("ch2Temperature", ch2Temperature);
                bundle.putString("ch1Brightness", ch1Brightness);
                bundle.putString("ch2Brightness", ch2Brightness);
                bundle.putString("deviceName", deviceName);
                bundle.putByte("maxType", maxType);
                ColorTemperatureFragment chFrag2 = new ColorTemperatureFragment();
                chFrag2.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag2);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case "curtain":
                CurtainNoDegreeFragment chFrag3 = new CurtainNoDegreeFragment();
                CurtainFragment chFrag4 = new CurtainFragment();
                BlindsFragment chFrag5 = new BlindsFragment();
                if (ch1CurtainType != null || ch2CurtainType != null) {
                    if (chNum == 1) {
                        if (ch1CurtainType.equals("00")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if (ch1CurtainType.equals("11")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);

                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if (ch1CurtainType.equals("10")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    }
                    if (chNum == 2) {
                        if (ch2CurtainType.equals("00")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if (ch2CurtainType.equals("11")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        if (ch2CurtainType.equals("10")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    }
                }
                    break;


        }

    }

    //根據按鈕來顯示CH對應的畫面
    public void defineBtnOnScreen(String chType, int chNum, String light) {
        Bundle bundle = new Bundle();
        byte maxType = 0x16;
        switch (chType) {
            case "dim":
                bundle.putInt("chNum", chNum);
                bundle.putByteArray("deviceSN", deviceSN);
                bundle.putByte("maxType", maxType);
                bundle.putString("ch1Brightness", light);
                bundle.putString("ch2Brightness", light);
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
                bundle.putString("deviceName", deviceName);
                ColorTemperatureFragment chFrag2 = new ColorTemperatureFragment();
                chFrag2.setArguments(bundle);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, chFrag2);
                fragmentTransaction.commit();
                break;
            case "curtain":
                CurtainNoDegreeFragment chFrag3 = new CurtainNoDegreeFragment();
                CurtainFragment chFrag4 = new CurtainFragment();
                BlindsFragment chFrag5 = new BlindsFragment();
                if (ch1CurtainType != null || ch2CurtainType != null) {
                    if (chNum == 1) {
                        if (ch1CurtainType.equals("00")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                            fragmentTransaction.commit();
                        }
                        if (ch1CurtainType.equals("11")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);
                            fragmentTransaction.commit();
                        }
                        if (ch1CurtainType.equals("10")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commit();
                        }
                    }
                    if (chNum == 2) {
                        if (ch2CurtainType.equals("00")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag3.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag3);
                            fragmentTransaction.commit();
                        }
                        if (ch2CurtainType.equals("11")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag4.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag4);
                            fragmentTransaction.commit();
                        }
                        if (ch2CurtainType.equals("10")) {
                            bundle.putInt("chNum", chNum);
                            bundle.putByteArray("deviceSN", deviceSN);
                            bundle.putByte("maxType", maxType);
                            bundle.putString("deviceName", deviceName);
                            bundle.putString("deviceSubtitle",subtitleText);
                            bundle.putString("type","16");
                            bundle.putInt("page",2);
                            chFrag5.setArguments(bundle);
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, chFrag5);
                            fragmentTransaction.commit();
                        }

                    }
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
                    Log.d(TAG, "onMax2Receive: " + hex);
                    Log.d(TAG, "長度: "+resultBytes.length);
                    /*if(Arrays.equals(resultBytes, statusMsg)){
                        Log.d(TAG, "r"+UDP.byteArrayToHexStr(resultBytes));
                        Log.d(TAG, "S: "+UDP.byteArrayToHexStr(statusMsg));
                    }*/
                    if (Arrays.equals(resultBytes, statusMsg)&&chInfo==null) {
                        if (failedCount ==1  && showDialog &&chInfo==null) {
                            new AlertDialog.Builder(Max2SettingActivity.this)
                                    .setTitle("裝置沒有回應")
                                    .setMessage("裝置沒有回應，請確認手機是否與裝置在同一個WiFi")
                                    .setPositiveButton("OK", null)
                                    .show();
                            showDialog = false;
                            loadingAni.setVisibility(View.GONE);
                            Log.d(TAG, "MaxScene沒有回應");

                        }
                    }
                    if (!hex.contains("1606") && resultBytes.length == 25||resultBytes.length==50) {
                        resultSN = hex.substring(32, 44);//回傳指令的裝置SN
                        //   Log.d(TAG, "長度: "+resultBytes.length);
                         Log.d(TAG, "裝置SN"+resultSN+"deviceSN:"+UDP.bytesToHex(deviceSN));
                    }
                    if (resultSN.equals(UDP.bytesToHex(deviceSN))) {
                        chInfo = hex.substring(14, 16);//指令第7個Byte(轉成字串第14 15個)
                        chInfo = UDP.hexToBin(chInfo);
                        //睡眠狀態判斷
                        sleepInfo = hex.substring(12,14);
                        sleepInfo = UDP.hexToBin(sleepInfo);
                        String ch1Sleep = sleepInfo.substring(4,6);
                        String ch2Sleep = sleepInfo.substring(6);
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
                       /* if(!ch1Sleep.equals("00") && !ch2Sleep.equals("00")){
                            startAni = true;
                            startAni2 = true;
                            if(ch1Sleep.equals("01")){
                                setChButtonOnAni(ch1Up);
                            }
                            if(ch1Sleep.equals("10")){
                                setChButtonOffAni(ch1Down);
                            }
                            if(ch2Sleep.equals("01")){
                                setChButtonOnAni2(ch2Up);
                            }
                            if(ch2Sleep.equals("10")){
                                setChButtonOffAni2(ch2Down);
                            }
                        }
                       else if(ch1Sleep.equals("01")){
                            startAni = true;
                            startAni2 = false;
                            setChButtonOnAni(ch1Up);
                        }
                       else if(ch1Sleep.equals("10") ){
                            startAni = true;
                            startAni2 = false;
                            setChButtonOffAni(ch1Down);
                        }
                       else if(ch1Sleep.equals("00") ){
                            startAni = false;
                            ch1Up.clearAnimation();
                            ch1Down.clearAnimation();
                        }
                       else if(ch2Sleep.equals("01")){
                            startAni2 = true;
                            startAni = false;
                            setChButtonOnAni2(ch2Up);
                        }
                      else   if(ch2Sleep.equals("10")){
                            startAni2 = true;
                            startAni = false;
                            setChButtonOnAni2(ch2Down);
                        }
                       else if(ch2Sleep.equals("00")){
                           startAni2 = false;
                            ch2Up.clearAnimation();
                            ch2Down.clearAnimation();
                        }*/


                        ch1Type = chInfo.substring(4, 6);
                        ch1Type = defineChType(ch1Type);
                        ch1Brightness = hex.substring(4, 8);

                        String curtainInfo = hex.substring(16, 18);
                        curtainInfo = UDP.hexToBin(curtainInfo);
                        ch1CurtainType = curtainInfo.substring(4,6);
                        ch2CurtainType = curtainInfo.substring(6);

                        ch1Temperature = hex.substring(20, 22);
                        ch2Type = chInfo.substring(6);
                        ch2Type = defineChType(ch2Type);
                        ch2Brightness = hex.substring(0, 4);
                        ch2Temperature = hex.substring(18, 20);
                        //settingBtnStatus();
                        //Log.d(TAG, "CH2亮度: " + ch2Brightness);
                        Log.d(TAG, "CH1亮度: " + ch1Brightness);
                        settingBtnStatus();
                        // Log.d(TAG, "CH2色溫: " + ch2Temperature);
                        /*Bundle chBundle = new Bundle();
                        byte[] ch1Brightness = {0x19,0x05,(byte) 0xAA,0x03};
                        chBundle.putByteArray("ch1");*/
                        if (defaultUi) {
                            if(deviceChNum==0) {
                                defineChOnScreen(ch1Type, 1);
                                loadingAni.setVisibility(View.GONE);
                                ch1Button.setEnabled(true);
                                ch2Button.setEnabled(true);
                                ch1Up.setEnabled(true);
                                ch2Up.setEnabled(true);
                                ch2Down.setEnabled(true);
                                ch1Down.setEnabled(true);
                                defaultUi = false;
                                //從捷徑進來的邏輯
                            } else {
                                if(deviceChNum==1){
                                    defineChOnScreen(ch1Type, deviceChNum);
                                    ch1Button.setSelected(true);
                                    ch2Button.setSelected(false);
                                }
                                if(deviceChNum==2){
                                    defineChOnScreen(ch2Type, deviceChNum);
                                    ch1Button.setSelected(false);
                                    ch2Button.setSelected(true);
                                }
                                loadingAni.setVisibility(View.GONE);
                                ch1Button.setEnabled(true);
                                ch2Button.setEnabled(true);
                                ch1Up.setEnabled(true);
                                ch2Up.setEnabled(true);
                                ch1Down.setEnabled(true);
                                ch2Down.setEnabled(true);
                                defaultUi = false;
                            }
                        }
                        //  Log.d(TAG, "ch1: " + ch1Type + " ch2: " + ch2Type);
                    }

                    break;

            }
        }
    }

    //顯示上方按鈕的UI
    private void settingBtnStatus() {
        int colorRed = ContextCompat.getColor(Max2SettingActivity.this, R.color.red);
        int colorGray = ContextCompat.getColor(Max2SettingActivity.this, R.color.gray2);
        int colorBlue = ContextCompat.getColor(Max2SettingActivity.this, R.color.blue);
        int num1 = Integer.parseInt(ch1Brightness, 16);
        float percent1 = (float) num1 / 1023;
        int num2 = Integer.parseInt(ch2Brightness, 16);
        float percent2 = (float) num2 / 1023;
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
        if (percent1 != 1.0f && percent1 != 0.0f) {
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
        }
        if (percent2 != 1.0f && percent2 != 0.0f) {
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
        }

    }
}



       /* //擴充功能
        final Button hiddenButton = findViewById(R.id.expand_button);
        final CardView cardView = findViewById(R.id.cardview);
        final ConstraintLayout hiddenView = findViewById(R.id.hidden_view);
        //TimePicker設定
        final TimePicker timePicker = findViewById(R.id.timepicker);
        final TextView timeText = findViewById(R.id.setting_timing);
        timePicker.setIs24HourView(true);*/



       /* timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeText.setText(timePicker.getHour()+"hrs   " + timePicker.getMinute()+"min");
            }
        });

        final TextView percentText = findViewById(R.id.percent);*/



       /* //seekbar設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentText.setText(progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/


    /*//卡片擴充按鍵
        hiddenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If the CardView is already expanded, set its visibility
                //  to gone and change the expand less icon to expand more.
                if (hiddenView.getVisibility() == View.VISIBLE) {

                    // The transition of the hiddenView is carried out
                    //  by the TransitionManager class.
                    // Here we use an object of the AutoTransition
                    // Class to create a default transition.
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    hiddenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_more_24,0,0,0);
                }

                // If the CardView is not expanded, set its visibility
                // to visible and change the expand more icon to expand less.
                else {

                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.VISIBLE);
                    hiddenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_less_24,0,0,0);
                }
            }
        });

    }*/
  /*  public void ch1ButtonSelected(View view){
        ch1Button.setSelected(true);
        ch2Button.setSelected(false);
    }
    public void ch2ButtonSelected(View view){
        ch1Button.setSelected(false);
        ch2Button.setSelected(true);
    }*/






