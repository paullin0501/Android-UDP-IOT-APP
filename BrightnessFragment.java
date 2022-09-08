package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class BrightnessFragment extends Fragment {
    int chNum; //代表從哪個CH開啟的
    private static final String TAG = "brightness";
    DeviceCommand deviceCommand = new DeviceCommand();
    byte[] deviceSN;
    byte[] chData;//指令(還不包括亮度與SN)
    byte maxType;//控制的裝置類型
    String chInfo; //用來判斷裝置的種類
    boolean controlling = false; //判斷使用者是否在控制
    boolean off = true; //判斷是否按下Off按鈕
    String ch1Brightness;//代表目前進來的CH當前的亮度
    String ch2Brightness;//代表目前進來的CH當前的亮度
    String ch3Brightness;//代表目前進來的CH當前的亮度
    String ch1Sleep = "00";
    String ch2Sleep = "00";
    String ch3Sleep = "00";
    private boolean canceling = false;
    private Button  hiddenButton;
    private Button  cancelSleepButton;
    String deviceName;
    int currentBrightness;
    MaterialButton shortcutBtn;
    Handler handler = new Handler();
    List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    public static Handler handlerSeekBar = new Handler();
    public  static Handler cancelHandler = new Handler();
    Runnable runnable;
    boolean isStarted=false; //判斷狀態更新用
    boolean sleepOrBright=false; //true代表漸亮
    ExecutorService exec = Executors.newCachedThreadPool();
    int dimStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brightness, container, false);
    }
   /* public void passData(boolean data) {
        dataPasser.onDataPass(data);
    }*/
  /*  @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }*/

    private void sendData(){

    }

    //fragment開始的方法
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SeekBar seekBar = view.findViewById(R.id.brightness_seekBar);
        //初始化設定
        Bundle bundle = getArguments();
        chNum = bundle.getInt("chNum");
        deviceSN = bundle.getByteArray("deviceSN");
        maxType = bundle.getByte("maxType");
        deviceName = bundle.getString("deviceName");
        //初始化shortcut
        Shortcut shortcut = new Shortcut();
        shortcut.setIcon(R.drawable.brightness_icon);
        String deviceCH = "CH"+chNum;

        chData = settingBrightnessCMD(chNum);

        new Thread(() -> {
            List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
            shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByhostSNandTypeandCH(UDP.byteArrayToHexStr(deviceSN),  String.format("%02X ", maxType),"CH" + chNum);
            ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateTypeWithID(shortcutCheckData.get(0).getId(), "MAX-1CT");
        }).start();

        //定義捷徑按鍵
        shortcutBtn = view.findViewById(R.id.shortcutButton);
        new Thread(() -> {

            shortcutData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN), "CH" + chNum,String.format("%02X ", maxType),true);
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(shortcutData.size()>0){
                        int colorGray = ResourcesCompat.getColor(getResources(), R.color.gray2, null);
                        shortcutBtn.setText("Remove shortcut");
                        shortcutBtn.setTextColor(colorGray);
                        shortcutBtn.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.minus_btn_size, null));
                        shortcutBtn.setIconTintResource(R.color.gray2);
                    }
                }
            });

        }).start();
        shortcutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = shortcutBtn.getText().toString();
                int colorGray = ResourcesCompat.getColor(getResources(), R.color.gray2, null);
                int colorOrange = ResourcesCompat.getColor(getResources(), R.color.orange, null);

                if(buttonText.equals("Add to shortcut")){
                    shortcutBtn.setText("Remove shortcut");
                    shortcutBtn.setTextColor(colorGray);
                    shortcutBtn.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.minus_btn_size, null));
                    shortcutBtn.setIconTintResource(R.color.gray2);
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData =  ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN),"CH"+chNum,String.format("%02X ", maxType),false);
                        ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateShowandicon(shortcutCheckData.get(0).getId(),shortcut.getIcon(),true);
                    }).start();
                    Toast.makeText(getActivity(),"Add to shortcut successful" ,Toast.LENGTH_SHORT).show();
                } else {
                    shortcutBtn.setText("Add to shortcut");
                    shortcutBtn.setTextColor(colorOrange);
                    shortcutBtn.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.add_btn_size, null));
                    shortcutBtn.setIconTintResource(R.color.orange);
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData =  ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN),"CH"+chNum,String.format("%02X ", maxType),true);
                        int id = shortcutCheckData.get(0).getId();
                        ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateShowandicon(id,shortcut.getIcon(),false);
                    }).start();

                    Toast.makeText(getActivity(),"Remove shortcut successful" ,Toast.LENGTH_SHORT).show();
                }
            }
        });


        ch1Brightness = bundle.getString("ch1Brightness");
        ch2Brightness = bundle.getString("ch2Brightness");
        ch3Brightness = bundle.getString("ch3Brightness");
        switch (chNum){
            case 1:
                int num = Integer.parseInt(ch1Brightness, 16);
                float percent = (float) num / 1023;
                percent = percent * 100;
                currentBrightness = num;
                break;
            case 2:
                int num1 = Integer.parseInt(ch2Brightness, 16);
                float percent1 = (float) num1 / 1023;
                percent1 = percent1 * 100;
                currentBrightness = num1 ;
                break;
            case 3:
                int num2 = Integer.parseInt(ch2Brightness, 16);
                float percent2 = (float) num2 / 1023;
                percent2 = percent2 * 100;
                currentBrightness = num2;
                break;
        }

        //擴充功能
        hiddenButton = view.findViewById(R.id.expand_button);
        cancelSleepButton = view.findViewById(R.id.cancel_sleep_button);
        hiddenButton.setText("Dimming Mode Setting");
        final CardView cardView = view.findViewById(R.id.cardview);
        final ConstraintLayout hiddenView = view.findViewById(R.id.hidden_view);
        //TimePicker設定
        final TimePicker timePicker = view.findViewById(R.id.timepicker);
        final TextView timeText = view.findViewById(R.id.setting_timing);
        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(0);
        //初始化sleeping mode按鍵
        final Button ok_btn = view.findViewById(R.id.ok_button1);
        final TextView off_btn = view.findViewById(R.id.offButton);
        final TextView on_btn = view.findViewById(R.id.onButton);
        final Button reset_btn = view.findViewById(R.id.reset_button1);
//睡眠取消按鈕
        cancelSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*cancelSleepButton.setVisibility(View.GONE);
                hiddenButton.setText("Dimming Mode Setting");*/
                if(chNum==1) {
                    if(ch1Sleep.equals("01")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, true, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                    if(ch1Sleep.equals("10")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, false, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                }
                if(chNum==2) {
                    if(ch2Sleep.equals("01")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, true, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                    if(ch2Sleep.equals("10")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, false, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                }
                if(chNum==3) {
                    if(ch3Sleep.equals("01")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, true, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                    if(ch3Sleep.equals("10")) {
                        byte[] cmd1 = deviceCommand.settingMaxLiteSleepCMD(chNum, false, maxType);
                        byte[] cmd = settingSleepingCancelCMD(cmd1);
                        maxLiteCommand(cmd);
                    }
                }
              /*  Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        canceling = true;
                    }
                };
               cancelHandler.postDelayed(r, 1000);*/
                canceling = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       canceling = false;
                    }
                }, 1000);
                cancelSleepButton.setVisibility(View.GONE);
                hiddenButton.setText("Dimming Mode Setting");
            }
        });


        off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                off = false;
                controlling = false;
                byte[] cmd;
                byte[] sendCMD;
                cmd = deviceCommand.settingMaxLiteBtnCMD(chNum,false,maxType);
                sendCMD = deviceCommand.settingCMD(cmd, maxType, deviceSN);
                maxLiteCommand(sendCMD);
            }
        });
        on_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                off = false;
                controlling = false;
                byte[] cmd;
                byte[] sendCMD;
               cmd = deviceCommand.settingMaxLiteBtnCMD(chNum,true,maxType);
               sendCMD = deviceCommand.settingCMD(cmd, maxType, deviceSN);
               maxLiteCommand(sendCMD);
            }
        });

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               int min =  timePicker.getMinute();
               int hour = timePicker.getHour() * 60 ;
               int totalTime = min + hour;
                String selectedText = null;
                RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
                RadioButton bright_btn = (RadioButton) radioGroup.findViewById(R.id.bright_btn);
                RadioButton sleep_btn = (RadioButton) radioGroup.findViewById(R.id.sleep_btn);

                if (radioButton.getId()== sleep_btn.getId()){
                     selectedText = "sleep";
                     chData = deviceCommand.settingMaxLiteSleepCMD(chNum,false,maxType);
                     dimStatus = 0;
                }
                if(radioButton.getId()== bright_btn.getId()){
                    selectedText = "bright";
                    chData = deviceCommand.settingMaxLiteSleepCMD(chNum,true,maxType);
                    dimStatus = 1;
                }
                String minHex = String.format("%1$04X", totalTime);
                Log.d(TAG, "time: "+minHex);
                byte[] cmd =  settingSleepingCMD(chData,minHex);
                maxLiteCommand(cmd);

                RxBus.getInstance().publish("sleepingMode",chNum,dimStatus);

               Toast.makeText(getActivity(),hour/60+"hours " + min + "minutes to " + selectedText ,Toast.LENGTH_SHORT).show();
                    //關閉sleeping mode ui
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    hiddenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_more_24, 0, 0, 0);
            }
        });
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.setHour(0);
                timePicker.setMinute(0);
                RadioButton bright_btn = view.findViewById(R.id.bright_btn);
                RadioButton sleep_btn = view.findViewById(R.id.sleep_btn);
                bright_btn.setChecked(false);
                sleep_btn.setChecked(false);
            }
        });
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        BroadcastReceiver MyBroadcast = new BroadcastReceiver() {
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
                      //  Log.d(TAG, "get: "+resultBytes);
                        String hex = UDP.byteArrayToHexStr(resultBytes);
                        Log.d(TAG, "onReceive: "+hex);
                        if (!hex.contains("1606") && (resultBytes.length == 25 || resultBytes.length==50) && !hex.contains("1406") && !hex.contains("1806")) {
                            resultSN = hex.substring(32, 44);//回傳指令的裝置SN
                            //   Log.d(TAG, "長度: "+resultBytes.length);
                           //  Log.d(TAG, "裝置SN"+resultSN+"deviceSN:"+UDP.bytesToHex(deviceSN));
                        }

                        if (resultSN.equals(UDP.bytesToHex(deviceSN))&& controlling==false) {


                            if(maxType==0x16){
                                ch1Brightness = hex.substring(4, 8);
                                ch2Brightness = hex.substring(0, 4);
                             //   Log.d(TAG, "ch1Brightness: "+ch1Brightness);
                                //睡眠模式狀態顯示
                                String sleepInfo = hex.substring(12,14);
                                sleepInfo = UDP.hexToBin(sleepInfo);
                                ch1Sleep = sleepInfo.substring(4,6);
                                ch2Sleep = sleepInfo.substring(6);
                            }
                            if(maxType==0x18){
                                ch1Brightness = hex.substring(8, 12);
                                ch2Brightness = hex.substring(4, 8);
                                ch3Brightness = hex.substring(0, 4);
                                //睡眠模式狀態顯示
                                String sleepInfo = hex.substring(12,14);
                                sleepInfo = UDP.hexToBin(sleepInfo);
                                ch1Sleep = sleepInfo.substring(2,4);
                                ch2Sleep = sleepInfo.substring(4,6);
                                ch3Sleep = sleepInfo.substring(6);
                            }
                            if(maxType==0x14){
                                ch1Brightness = hex.substring(0, 4);
                                //睡眠模式狀態顯示
                                String sleepInfo = hex.substring(12,14);
                                sleepInfo = UDP.hexToBin(sleepInfo);
                                ch1Sleep = sleepInfo.substring(6);
                            }

                           /* switch (chNum){
                                case 1:
                                    int num = Integer.parseInt(ch1Brightness, 16);
                                    float percent = (float) num / 1023;
                                    percent = percent * 100;
                                    currentBrightness = Math.round(percent) ;
                                case 2:
                                    int num1 = Integer.parseInt(ch2Brightness, 16);
                                    float percent1 = (float) num1 / 1023;
                                    percent1 = percent1 * 100;
                                    currentBrightness = Math.round(percent1) ;
                                case 3:
                                    int num2 = Integer.parseInt(ch3Brightness, 16);
                                    float percent2 = (float) num2 / 1023;
                                    percent2 = percent2* 100;
                                    currentBrightness = Math.round(percent2) ;
                            }*/
                            //Log.d(TAG, "chNum: "+chNum);
                            if(chNum==1){
                                int num = Integer.parseInt(ch1Brightness, 16);
                                float percent = (float) num / 1023;
                                 percent = percent * 100;
                                 currentBrightness = num;
                                Log.d(TAG, "current: "+currentBrightness);
                                 seekBar.setProgress(num);
                                 if(num!=0){
                                     off = false;
                                 }
                                if(num==0){
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                //睡眠模式狀態顯示
                                if(!canceling) {
                                    if (ch1Sleep.equals("01")) {
                                        hiddenButton.setText("Dimming Mode: Dim up");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch1Sleep.equals("10")) {
                                        hiddenButton.setText("Dimming Mode: Dim down");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch1Sleep.equals("00")) {
                                        hiddenButton.setText("Dimming Mode Setting");
                                        cancelSleepButton.setVisibility(View.GONE);
                                    }
                                }

                            }
                            if(chNum==2){
                                int num = Integer.parseInt(ch2Brightness, 16);
                                float percent = (float) num / 1023;
                                percent = percent * 100;
                                currentBrightness = num;
                                if(num!=0){
                                    off = false;
                                }
                                if(num==0){
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                seekBar.setProgress(num);
                                //睡眠模式狀態顯示
                                if(!canceling) {
                                    if (ch2Sleep.equals("01")) {
                                        hiddenButton.setText("Dimming Mode: Dim up");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch2Sleep.equals("10")) {
                                        hiddenButton.setText("Dimming Mode: Dim down");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch2Sleep.equals("00")) {
                                        hiddenButton.setText("Dimming Mode Setting");
                                        cancelSleepButton.setVisibility(View.GONE);
                                    }
                                }


                            }
                            if(chNum==3){
                                int num = Integer.parseInt(ch3Brightness, 16);
                                float percent = (float) num / 1023;
                                percent = percent * 100;
                                if(num!=0){
                                    off = false;
                                }
                                if(num==0){
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                currentBrightness =num;
                                seekBar.setProgress(num);
                                //睡眠模式狀態顯示
                                if(!canceling) {
                                    if (ch3Sleep.equals("01")) {
                                        hiddenButton.setText("Dimming Mode: Dim up");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch3Sleep.equals("10")) {
                                        hiddenButton.setText("Dimming Mode: Dim down");
                                        cancelSleepButton.setVisibility(View.VISIBLE);
                                    }
                                    if (ch3Sleep.equals("00")) {
                                        hiddenButton.setText("Dimming Mode Setting");
                                        cancelSleepButton.setVisibility(View.GONE);
                                    }
                                }

                            }
                        }
                        break;
                }
            }
        };
        getActivity().registerReceiver(MyBroadcast, intentFilter);
/*
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "run: "+123);

            }
        }, 5000);*/


        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeText.setText(timePicker.getHour() + "hrs   " + timePicker.getMinute() + "min");
            }
        });

        final TextView percentText = view.findViewById(R.id.percent);


        //seekbar設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percent =  ((float) progress/1023)*100;
                //將結果轉成小數點後一位
                DecimalFormat df = new DecimalFormat("0.0");
                percentText.setText( df.format(percent)+ "%");
                if(progress < 3 && !off){
                    percentText.setText( 0.3+ "%");
                    //將值設回3才是正確的UI
                    seekBar.setProgress(3);
                }




            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                controlling = true;
                off = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Runnable r = new Runnable() {

                    @Override
                    public void run() {

                        controlling = false;
                        canceling = false;

                    }
                };
                handlerSeekBar.removeCallbacks(r);
                handlerSeekBar.postDelayed(r, 2000);

            }
        });

        if (ch1Brightness != null&& chNum==1) {
            int num = Integer.parseInt(ch1Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness =num;
            //  Log.d(TAG, "onViewCreated: "+seekBar.getProgress());
            Log.d(TAG, "CH1亮度: " + ch1Brightness);
                Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        seekBar.setMax(1023);

                    }
                }, 200);

        }
        if (ch2Brightness != null&& chNum==2) {

            int num = Integer.parseInt(ch2Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness =num;
            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {

                @Override
                public void run() {
                    seekBar.setMax(1023);

                }
            }, 200);
        }

        if (ch3Brightness != null&& chNum==3) {

            int num = Integer.parseInt(ch3Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness =num;
            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {

                @Override
                public void run() {
                    seekBar.setMax(1023);

                }
            }, 200);

        }
        //卡片擴充按鍵
        hiddenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenView.getVisibility() == View.VISIBLE) {

                    // The transition of the hiddenView is carried out
                    //  by the TransitionManager class.
                    // Here we use an object of the AutoTransition
                    // Class to create a default transition.
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    hiddenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_more_24, 0, 0, 0);
                }

                // If the CardView is not expanded, set its visibility
                // to visible and change the expand more icon to expand less.
                else {

                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.VISIBLE);
                    hiddenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_less_24, 0, 0, 0);
                }
            }
        });
        //設定計時發送調光的指令 才不會造成滑動UI時傳送過多指令
        runnable = new Runnable() {
            @Override
            public void run() {
                int progress = seekBar.getProgress();
                if (currentBrightness != progress) {
                    // Log.d(TAG, "目前亮度: "+ch1Brightness);
                    chData = settingBrightnessCMD(chNum);
                    byte cmd[];
                    cmd = settingCMD(chData, progress);
                    RxBus.getInstance().publish("stop",chNum);
                    currentBrightness = progress;
                    maxLiteCommand(cmd);
                }
                //Log.d(TAG, "run: "+progress);
               // Log.d(TAG, "current: "+currentBrightness);
                handler.postDelayed(this, 1000);
            }
        };
        runnable.run(); //執行計時器
    }

    @Override
    public void onStop() {
        super.onStop();
      //  handler.removeCallbacks(runnable);
    }
    //根據設定時間轉成睡眠模式的指令
    private byte[] settingSleepingCMD(byte[] chData, String minute) {
        byte[] liteCHCmd = {maxType, 0x06};
        byte[] minCmd = UDP.hexToByte(minute);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData,minCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }
    //根據設定時間轉成睡眠模式的指令
    private byte[] settingSleepingCancelCMD(byte[] chData) {
        byte[] liteCHCmd = {maxType, 0x06};
        byte[] lastCmd = {(byte)0x80,0x00};
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, lastCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }

    //根據調整的比例轉成相對應的指令
    private byte[] settingCMD(byte[] chData, int percent) {
        byte[] liteCHCmd = {maxType, 0x06};
        float percentNum;
       // percentNum = (float) percent / 100;
       // int brightnessNum;
       // brightnessNum =Math.round((1023 * percentNum));
        Log.d(TAG, "控制亮度: "+percent);
        // Log.d(TAG, "目前亮度: " + brightnessNum);
        //String percentHex =  Integer.toHexString(brightnessNum);
        String percentHex = String.format("%1$03X", percent);
        int num = 8;
        String title = Integer.toHexString(num);
        percentHex = title + percentHex;
        // Log.d(TAG, "目前亮度: " + percentHex);
        byte[] percentCmd = UDP.hexToByte(percentHex);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, percentCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
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

    //根據CH來定義指令
    public byte[] settingBrightnessCMD(int chNum) {
        byte[] dataTemp = new byte[]{};
        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE};
                    dataTemp = dataTemp3;
                    break;
            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAC};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAE};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAA};
                    dataTemp = dataTemp3;
                    break;
            }
        }
        if(maxType==0x14) {
            switch (chNum) {
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAA};
                    dataTemp = dataTemp2;
                    break;
            }
        }
        return dataTemp;
    }
    
}
