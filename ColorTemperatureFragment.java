package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.icu.text.DecimalFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorTemperatureFragment extends Fragment {
    int chNum; //代表從哪個CH開啟的
    private static final String TAG = "temperature";
    DeviceCommand deviceCommand = new DeviceCommand();
    byte[] deviceSN;
    byte[] chDataBrightness;//指令(還不包括亮度與SN)
    byte[] chDataTemperature;//指令(還不包括亮度與SN)
    byte[] chDataSleep;
    byte maxType;//控制的裝置類型
    int currentBrightness;
    int currentTemperature;
    String ch1Temperature;//代表目前進來的CH當前的色溫
    String ch1Brightness;//代表目前進來的CH當前的亮度
    String ch2Temperature;//代表目前進來的CH當前的色溫
    String ch2Brightness;//代表目前進來的CH當前的亮度
    String ch3Brightness;
    String ch1Sleep ="00";
    String ch2Sleep="00";
    String ch3Sleep="00";
    private boolean canceling = false;
    private Button  hiddenButton;
    private Button  cancelSleepButton;
    String ch3Temperature;
    int dimStatus;
    String deviceName;
    MaterialButton shortcutBtn;
    List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    public  static Handler cancelHandler = new Handler();
    boolean controlling = false; //判斷使用者是否在控制
    boolean off = true; //判斷是否按下Off按鈕
    Handler handler = new Handler();
    Runnable runnable;
    Handler handler1 = new Handler();
    Runnable runnable1;
    ExecutorService exec = Executors.newCachedThreadPool();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_color_temperature, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SeekBar seekBar = view.findViewById(R.id.brightness2_seekBar);
        SeekBar temperatureSeekBar = view.findViewById(R.id.temperature_seekBar);
        Bundle bundle = getArguments();
        //初始化設定
        chNum = bundle.getInt("chNum");
        //Log.d(TAG, "temper: "+chNum);
        deviceSN = bundle.getByteArray("deviceSN");
        maxType = bundle.getByte("maxType");
        ch1Brightness = bundle.getString("ch1Brightness");
        ch1Temperature = bundle.getString("ch1Temperature");
        ch2Brightness = bundle.getString("ch2Brightness");
        ch2Temperature = bundle.getString("ch2Temperature");
        ch3Brightness = bundle.getString("ch3Brightness");
        ch3Temperature = bundle.getString("ch3Temperature");

        chDataBrightness = settingBrightnessDataCMD(chNum);
        chDataTemperature = settingColortemperatureDataCMD(chNum);

        //初始化shortcut
        deviceName = bundle.getString("deviceName");
        Shortcut shortcut = new Shortcut();
        shortcut.setIcon(R.drawable.temperature_icon);

        new Thread(() -> {
            List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
            shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByhostSNandTypeandCH(UDP.byteArrayToHexStr(deviceSN),  String.format("%02X ", maxType),"CH" + chNum);
            ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateTypeWithID(shortcutCheckData.get(0).getId(), "MAX-2CT");
        }).start();
        switch (chNum) {
            case 1:
                int num = Integer.parseInt(ch1Brightness, 16);
                float percent = (float) num / 1023;
                percent = percent * 100;
                if (ch1Temperature != null) {
                    int temp = Integer.parseInt(ch1Temperature, 16);
                    currentTemperature = temp;
                }
                currentBrightness = Math.round(percent);
                break;
            case 2:
                int num1 = Integer.parseInt(ch2Brightness, 16);
                float percent1 = (float) num1 / 1023;
                percent1 = percent1 * 100;
                if (ch2Temperature != null) {
                    int temp1 = Integer.parseInt(ch2Temperature, 16);
                    currentTemperature = temp1;
                }

                // Log.d(TAG, "t2: "+currentTemperature);
                currentBrightness = Math.round(percent1);
                break;
            case 3:
                int num2 = Integer.parseInt(ch3Brightness, 16);
                float percent2 = (float) num2 / 1023;
                percent2 = percent2 * 100;
                if (ch3Temperature != null) {
                    int temp2 = Integer.parseInt(ch3Temperature, 16);
                    currentTemperature = temp2;
                }

                currentBrightness = Math.round(percent2);
                break;
        }

        //定義捷徑按鍵
        shortcutBtn = view.findViewById(R.id.shortcutButton);
        new Thread(() -> {

            shortcutData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN), "CH" + chNum, String.format("%02X ", maxType), true);
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (shortcutData.size() > 0) {
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

                if (buttonText.equals("Add to shortcut")) {
                    shortcutBtn.setText("Remove shortcut");
                    shortcutBtn.setTextColor(colorGray);
                    shortcutBtn.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.minus_btn_size, null));
                    shortcutBtn.setIconTintResource(R.color.gray2);
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN), "CH" + chNum, String.format("%02X ", maxType), false);
                        ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateShowandicon(shortcutCheckData.get(0).getId(), shortcut.getIcon(), true);
                    }).start();
                    Toast.makeText(getActivity(), "Add to shortcut successful", Toast.LENGTH_SHORT).show();
                } else {
                    shortcutBtn.setText("Add to shortcut");
                    shortcutBtn.setTextColor(colorOrange);
                    shortcutBtn.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.add_btn_size, null));
                    shortcutBtn.setIconTintResource(R.color.orange);
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(deviceSN), "CH" + chNum, String.format("%02X ", maxType), true);
                        int id = shortcutCheckData.get(0).getId();
                        ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateShowandicon(id, shortcut.getIcon(), false);
                    }).start();

                    Toast.makeText(getActivity(), "Remove shortcut successful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //擴充功能
        hiddenButton = view.findViewById(R.id.expand_button);
        cancelSleepButton = view.findViewById(R.id.cancel_sleep_button);
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
        final Button reset_btn = view.findViewById(R.id.reset_button1);
        final TextView off_btn = view.findViewById(R.id.offButton);
        final TextView on_btn = view.findViewById(R.id.onButton);
        hiddenButton.setText("Dimming Mode Setting");
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
            /*    Runnable r = new Runnable() {

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
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int min = timePicker.getMinute();
                int hour = timePicker.getHour() * 60;
                int totalTime = min + hour;
                String selectedText = null;
                RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio_group1);
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
                RadioButton bright_btn = (RadioButton) radioGroup.findViewById(R.id.bright_btn1);
                RadioButton sleep_btn = (RadioButton) radioGroup.findViewById(R.id.sleep_btn1);

                if (radioButton.getId() == sleep_btn.getId()) {
                    selectedText = "sleep";
                    chDataSleep = deviceCommand.settingMaxLiteSleepCMD(chNum, false, maxType);
                    dimStatus = 0;
                }
                if (radioButton.getId() == bright_btn.getId()) {
                    selectedText = "bright";
                    chDataSleep = deviceCommand.settingMaxLiteSleepCMD(chNum, true, maxType);
                    dimStatus = 1;
                }
                String minHex = String.format("%1$04X", totalTime);
                Log.d(TAG, "time: " + minHex);
                byte[] cmd = settingSleepingCMD(chDataSleep, minHex);
                Log.d(TAG, "sleep cmd: " + UDP.byteArrayToHexStr(cmd));
                maxLiteCommand(cmd);

                RxBus.getInstance().publish("sleepingMode", chNum,dimStatus);

                Toast.makeText(getActivity(), hour / 60 + "hours " + min + "minutes to " + selectedText, Toast.LENGTH_SHORT).show();
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
                RadioButton bright_btn = view.findViewById(R.id.bright_btn1);
                RadioButton sleep_btn = view.findViewById(R.id.sleep_btn1);
                bright_btn.setChecked(false);
                sleep_btn.setChecked(false);
            }
        });

        off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                off = false;
                controlling = false;
                byte[] cmd;
                byte[] sendCMD;
                cmd = deviceCommand.settingMaxLiteBtnCMD(chNum, false, maxType);
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
                cmd = deviceCommand.settingMaxLiteBtnCMD(chNum, true, maxType);
                sendCMD = deviceCommand.settingCMD(cmd, maxType, deviceSN);
                maxLiteCommand(sendCMD);
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeText.setText(timePicker.getHour() + "hrs   " + timePicker.getMinute() + "min");
            }
        });


        final TextView percentText = view.findViewById(R.id.percent);
        // final TextView temperatureText =  view.findViewById(R.id.temperature);
        //seekbar設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percent = ((float) progress / 1023) * 100;
                //將結果轉成小數點後一位
                DecimalFormat df = new DecimalFormat("0.0");
                percentText.setText(df.format(percent) + "%");
                if (progress < 3 && !off) {
                    percentText.setText(0.3 + "%");
                    //將值設回3才是正確的UI
                    seekBar.setProgress(3);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                controlling = true;
                off = false;
            }

            //等待停止滑動後  才會做狀態的更新  避免seekBar的跳動
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        controlling = false;
                    }
                }, 2000);
            }
        });
        startBrightnessSeekBar(seekBar);
        //色溫seekbar設定
        temperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //temperatureText.setText(progress + "K");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                controlling = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        controlling = false;
                    }
                }, 3000);
            }
        });
        startTemperatureSeekBar(temperatureSeekBar);


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

                        String hex = UDP.byteArrayToHexStr(resultBytes);
                        Log.d(TAG, "onReceive: " + hex);
                        if (!hex.contains("1606") && (resultBytes.length == 25 || resultBytes.length == 50) && !hex.contains("1406") && !hex.contains("1806")) {
                            resultSN = hex.substring(32, 44);//回傳指令的裝置SN
                            //   Log.d(TAG, "長度: "+resultBytes.length);
                            // Log.d(TAG, "裝置SN"+resultSN+"deviceSN:"+UDP.bytesToHex(deviceSN));
                        }

                        if (resultSN.equals(UDP.bytesToHex(deviceSN)) && controlling == false) {

                            if (maxType == 0x16) {
                                ch1Temperature = hex.substring(20, 22);
                                ch2Temperature = hex.substring(18, 20);
                                ch1Brightness = hex.substring(4, 8);
                                ch2Brightness = hex.substring(0, 4);
                                //睡眠模式狀態顯示
                                String sleepInfo = hex.substring(12,14);
                                sleepInfo = UDP.hexToBin(sleepInfo);
                                ch1Sleep = sleepInfo.substring(4,6);
                                ch2Sleep = sleepInfo.substring(6);
                            }
                            if (maxType == 0x18) {
                                ch1Temperature = hex.substring(22, 24);
                                ch2Temperature = hex.substring(20, 22);
                                ch3Temperature = hex.substring(18, 20);
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
                            if (maxType == 0x14) {
                                ch1Brightness = hex.substring(0, 4);
                                ch1Temperature = hex.substring(18, 20);
                                //睡眠模式狀態顯示
                                String sleepInfo = hex.substring(12,14);
                                sleepInfo = UDP.hexToBin(sleepInfo);
                                ch1Sleep = sleepInfo.substring(6);
                            }

                            //Log.d(TAG, "chNum: "+chNum);
                            if (chNum == 1) {
                                int num = Integer.parseInt(ch1Brightness, 16);
                                float percent = (float) num / 1023;
                                percent = percent * 100;
                                if (num != 0) {
                                    off = false;
                                }
                                if (num == 0) {
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                currentBrightness = num;
                                seekBar.setProgress(num);
                                int temp = Integer.parseInt(ch1Temperature, 16);
                                currentTemperature = temp;
                                temperatureSeekBar.setProgress(temp);

                               /* Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);

                                    }
                                }, 200);*/
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
                            if (chNum == 2) {
                                int num = Integer.parseInt(ch2Brightness, 16);
                                float percent = (float) num / 1023;
                                percent = percent * 100;
                                if (num != 0) {
                                    off = false;
                                }
                                if (num == 0) {
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                currentBrightness = num;
                                seekBar.setProgress(num);
                                //  Log.d(TAG, "current: "+currentTemperature);
                                int temp = Integer.parseInt(ch2Temperature, 16);
                                currentTemperature = temp;
                                temperatureSeekBar.setProgress(temp);
                                //    seekBar.setProgress((int) percent);
                               /* Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);

                                    }
                                }, 200);*/
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
                            if (chNum == 3) {
                                int num = Integer.parseInt(ch3Brightness, 16);
                                float percent = (float) num / 1023;
                                percent = percent * 100;
                                if (num != 0) {
                                    off = false;
                                }
                                if (num == 0) {
                                    off = true;
                                    seekBar.setProgress(0);
                                }
                                currentBrightness = num;
                                seekBar.setProgress(num);
                                int temp = Integer.parseInt(ch3Temperature, 16);
                                currentTemperature = temp;
                                temperatureSeekBar.setProgress(temp);
                                //    seekBar.setProgress((int) percent);
                               /* Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);

                                    }
                                }, 200);*/
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
        //設定計時發送調光的指令
        runnable = new Runnable() {
            @Override
            public void run() {
                int progress = seekBar.getProgress();
                if (currentBrightness != progress) {
                    byte cmd[];
                    cmd = settingBrightnessCMD(chDataBrightness, progress); //結合亮度組成指令
                    // currentBrightness = progress;
                    maxLiteCommand(cmd);
                    RxBus.getInstance().publish("stop", chNum);
                }
                //Log.d(TAG, "run: "+progress);
                handler.postDelayed(this, 1000);
            }
        };
        runnable.run(); //執行計時器

        //設定計時發送色溫的指令
        runnable1 = new Runnable() {
            @Override
            public void run() {
                int progress = temperatureSeekBar.getProgress();
                if (currentTemperature != progress) {
                    byte cmd[];
                    cmd = settingTemperatureCMD(chDataTemperature, progress);
                    //  currentTemperature = progress;
                    maxLiteCommand(cmd);
                    RxBus.getInstance().publish("stop", chNum);
                }
                //Log.d(TAG, "run: "+progress);
                handler1.postDelayed(this, 1000);
            }
        };
        runnable1.run(); //執行計時器
    }

    private void startBrightnessSeekBar(SeekBar seekBar) {
        if (ch1Brightness != null && chNum == 1) {

            int num = Integer.parseInt(ch1Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness = num;
            // Log.d(TAG, "亮度: "+ch1Brightness);
            Handler handler3 = new Handler();
            handler3.postDelayed(new Runnable() {

                @Override
                public void run() {
                    seekBar.setMax(1023);

                }
            }, 200);

        }
        if (ch2Brightness != null && chNum == 2) {

            int num = Integer.parseInt(ch2Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness = num;
            Handler handler3 = new Handler();
            handler3.postDelayed(new Runnable() {

                @Override
                public void run() {
                    seekBar.setMax(1023);

                }
            }, 200);

        }
        if (ch3Brightness != null && chNum == 3) {

            int num = Integer.parseInt(ch3Brightness, 16);
            float percent = (float) num / 1023;
            percent = percent * 100;
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            seekBar.setMax(num);
            seekBar.setProgress(num);
            currentBrightness = num;
            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {

                @Override
                public void run() {
                    seekBar.setMax(1023);

                }
            }, 200);

        }
    }

    private void startTemperatureSeekBar(SeekBar temperatureSeekBar) {
        if (ch1Temperature != null && chNum == 1) {
            int num = Integer.parseInt(ch1Temperature, 16);
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            temperatureSeekBar.setMax(num);
            temperatureSeekBar.setProgress(num);
            currentTemperature = num;
            // Log.d(TAG, "色溫: "+currentTemperature);
            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {

                @Override
                public void run() {
                    temperatureSeekBar.setMax(100);

                }
            }, 200);

        }
        if (ch2Temperature != null && chNum == 2) {

            int num = Integer.parseInt(ch2Temperature, 16);
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            Log.d(TAG, "num: " + num);
            temperatureSeekBar.setMax(num);
            temperatureSeekBar.setProgress(num);
            currentTemperature = num;
            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {

                @Override
                public void run() {
                    temperatureSeekBar.setMax(100);

                }
            }, 100);

        }
        if (ch3Temperature != null && chNum == 3) {

            int num = Integer.parseInt(ch3Temperature, 16);
            //seekbarUI設定上的BUG 需要先設定MAX值才會正確
            Log.d(TAG, "num: " + num);
            temperatureSeekBar.setMax(num);
            temperatureSeekBar.setProgress(num);
            currentTemperature = num;
            Handler handler5 = new Handler();
            handler5.postDelayed(new Runnable() {

                @Override
                public void run() {
                    temperatureSeekBar.setMax(100);

                }
            }, 100);

        }
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

    //根據設定時間轉成睡眠模式的指令
    private byte[] settingSleepingCMD(byte[] chData, String minute) {
        byte[] liteCHCmd = {maxType, 0x06};
        byte[] minCmd = UDP.hexToByte(minute);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, minCmd);
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

    //根據CH來定義指令
    public byte[] settingBrightnessDataCMD(int chNum) {
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
        if (maxType == 0x14) {
            switch (chNum) {
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAA};
                    dataTemp = dataTemp2;
                    break;
            }
        }
        return dataTemp;
    }

    //根據調整的比例轉成相對應的指令
    private byte[] settingBrightnessCMD(byte[] chData, int percent) {
        byte[] liteCHCmd = {maxType, 0x06};
        float percentNum;
        percentNum = (float) percent / 100;
        int brightnessNum;
        brightnessNum = Math.round(1023 * percentNum);
        //String percentHex =  Integer.toHexString(brightnessNum);
        String percentHex = String.format("%1$03X", percent);
        int num = 8;
        String title = Integer.toHexString(num);
        percentHex = title + percentHex;
        byte[] percentCmd = UDP.hexToByte(percentHex);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, percentCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }

    //根據調整的比例轉成相對應的指令
    private byte[] settingTemperatureCMD(byte[] chData, int percent) {
        byte[] liteCHCmd = {maxType, 0x06};
        float percentNum;
        percentNum = (float) percent / 100;
        int brightnessNum;
        brightnessNum = Math.round(100 * percentNum);//00~64
        //String percentHex =  Integer.toHexString(brightnessNum);
        String percentHex = String.format("%1$02X", brightnessNum);//轉換成二位數的16進位
        // Log.d(TAG, "目前色溫: "+percentHex);
        byte[] percentCmd = UDP.hexToByte(percentHex);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, percentCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }

    //根據CH來定義指令
    public byte[] settingColortemperatureDataCMD(int chNum) {
        byte[] dataTemp = new byte[]{};


        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x04};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x04};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x04};
                    dataTemp = dataTemp3;
                    break;

            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x04};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x04};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x04};
                    dataTemp = dataTemp3;
                    break;

            }
        }
        if (maxType == 0x14) {
            switch (chNum) {
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x04};
                    dataTemp = dataTemp2;
                    break;
            }
        }
        return dataTemp;
    }
}

