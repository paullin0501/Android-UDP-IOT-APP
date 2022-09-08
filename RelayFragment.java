package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RelayFragment extends Fragment {
    int chNum; //代表從哪個CH開啟的
    private static final String TAG = "relay";
    DeviceCommand deviceCommand = new DeviceCommand();
    byte[] deviceSN;
    byte[] chData;//指令(還不包括亮度與SN)
    byte maxType;//控制的裝置類型
    boolean key; //按鍵開關
    SeekBar seekBar;
    String ch1Brightness;//代表目前進來的CH當前的亮度
    String ch2Brightness;//代表目前進來的CH當前的亮度
    String ch3Brightness;//代表目前進來的CH當前的亮度
    String deviceName;
    MaterialButton shortcutBtn;
    List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    int currentBrightness;
    Handler handler = new Handler();
    Runnable runnable;
    boolean controlling = false; //判斷使用者是否在控制
    ExecutorService exec = Executors.newCachedThreadPool();
    boolean lightStatus = false; //判斷目前燈是開或關的狀態

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_relay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        seekBar = view.findViewById(R.id.curtain_seekBar);
        Bundle bundle = getArguments();
        chNum = bundle.getInt("chNum");
        deviceSN = bundle.getByteArray("deviceSN");
        maxType = bundle.getByte("maxType");
        ch1Brightness = bundle.getString("ch1Brightness");
        ch2Brightness = bundle.getString("ch2Brightness");
        ch3Brightness = bundle.getString("ch3Brightness");

        //初始化shortcut
        deviceName = bundle.getString("deviceName");
        Shortcut shortcut = new Shortcut();
        shortcut.setIcon(R.drawable.relay_icon);
        String deviceCH = "CH"+chNum;

        //判斷燈目前的狀態
        liteStatus();
        chData = settingRelayCMD(chNum,key);
        Log.d(TAG, "chData: "+chData);
        Log.d(TAG, "目前裝置: " + key);
        Log.d(TAG, "deviceSN: "+deviceSN);


        new Thread(() -> {
            List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
            shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByhostSNandTypeandCH(UDP.byteArrayToHexStr(deviceSN),  String.format("%02X ", maxType),"CH" + chNum);
            ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateTypeWithID(shortcutCheckData.get(0).getId(), "MAX-Relay");
        }).start();

        //seekbar設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controlling = true;
                int progress = seekBar.getProgress();
                if (!lightStatus & progress > 0) {
                    seekBar.setProgress(100);
                    key = true;
                   chData =  settingRelayCMD(chNum,key);
                    byte cmd[];
                    cmd = settingCMD(chData);
                    maxLiteCommand(cmd);
                    Log.d(TAG, "正確指令"+UDP.byteArrayToHexStr(cmd));
                    lightStatus = true;
                }
                 else if (lightStatus & progress < 100) {
                    seekBar.setProgress(0);
                    key = false;
                    chData = settingRelayCMD(chNum,key);
                    byte cmd[];
                    cmd = settingCMD(chData);
                    maxLiteCommand(cmd);
                    lightStatus = false;
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        controlling = false;
                    }
                }, 300);


            }
        });
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
      /*  if (key) {
            seekBar.setProgress(100);
        }
        if (!key){
            seekBar.setProgress(0);
        }*/
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        BroadcastReceiver MyBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mAction = intent.getAction();
                assert mAction != null;
                switch (mAction) {
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
                        // Log.d(TAG, "onReceive: "+hex);
                        if (!hex.contains("1606") && resultBytes.length == 25) {
                            resultSN = hex.substring(32, 44);//回傳指令的裝置SN
                            //   Log.d(TAG, "長度: "+resultBytes.length);
                            //  Log.d(TAG, "裝置SN"+resultSN+"deviceSN:"+UDP.bytesToHex(deviceSN));
                        }
                       // Log.d(TAG, "controlling: "+controlling);
                        Log.d(TAG, "onReceive: "+hex);
                        if (resultSN.equals(UDP.bytesToHex(deviceSN))) {
                            if(maxType==0x16){
                                ch1Brightness = hex.substring(4, 8);
                                ch2Brightness = hex.substring(0, 4);
                            }
                            if(maxType==0x18){
                                ch1Brightness = hex.substring(8, 12);
                                ch2Brightness = hex.substring(4, 8);
                                ch3Brightness = hex.substring(0, 4);
                            }
                            if(maxType==0x14){
                                ch1Brightness = hex.substring(0, 4);
                            }


                            if(chNum==1){
                                if(ch1Brightness.equals("0000")){
                                    seekBar.setProgress(0);
                                }
                                if(ch1Brightness.equals("03FF")){
                                    seekBar.setProgress(100);
                                }
                                /*Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);

                                    }
                                }, 200);*/
                            }
                            if(chNum==2){
                                if(ch2Brightness.equals("0000")){
                                    seekBar.setProgress(0);
                                }
                                if(ch2Brightness.equals("03FF")){
                                    seekBar.setProgress(100);
                                }
                                Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);

                                    }
                                }, 200);
                            }
                            if(chNum==3){
                                if(ch3Brightness.equals("0000")){
                                    seekBar.setProgress(0);
                                }
                                if(ch3Brightness.equals("03FF")){
                                    seekBar.setProgress(100);
                                }
                               /* Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        seekBar.setMax(100);
                                    }
                                }, 200);*/
                            }
                        }
                        break;
                }
            }
        };
        getActivity().registerReceiver(MyBroadcast, intentFilter);
    }

    private byte[] settingCMD(byte[] chData) {
        byte[] liteCHCmd = {maxType, 0x06};
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }
    private void liteStatus() {
        Log.d(TAG, "B2: "+ ch2Brightness);
        Log.d(TAG, "B3: "+ ch3Brightness);
        if(ch1Brightness!=null) {
            if (ch1Brightness.equals("0000") && chNum == 1) {
                seekBar.setProgress(0);
                key = false;
            }

            if (ch1Brightness.equals("03FF") && chNum == 1) {
                seekBar.setProgress(100);
                lightStatus = true;
                key = true;
            }
        }
        if(ch2Brightness!=null) {
            if (ch2Brightness.equals("0000") && chNum == 2) {
                seekBar.setProgress(0);
                key = false;
            }

            if (ch2Brightness.equals("03FF") && chNum == 2) {
                seekBar.setProgress(100);
                lightStatus = true;
                key = true;
            }
        }
       if(ch3Brightness!=null) {
           if (ch3Brightness.equals("0000") && chNum == 3) {
               seekBar.setProgress(0);
               key = false;
           }
           if (ch3Brightness.equals("03FF") && chNum == 3) {
               seekBar.setProgress(100);
               lightStatus = true;
               key = true;
           }
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

    //根據CH來定義指令(點擊按鍵)
    public byte[] settingRelayCMD(int chNum, boolean key) {
        byte[] dataTemp = new byte[]{};
        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {

                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA5, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA4, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA5, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA4, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA7, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA6, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                case 3:
                    if (!key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                        dataTemp = dataTemp3;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                        dataTemp = dataTemp3;
                        break;
                    }

            }
        }
            if (maxType == 0x14 && chNum == 1) {
                if (!key) {
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                    dataTemp = dataTemp2;
                }
                if (key) {
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                    dataTemp = dataTemp2;
                }

            }


        return dataTemp;
    }
}


    /* @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = view.inflate(R.layout.fragment_relay, container, false);
        SeekBar seekBar =findViewById(R.id.seekBar);
        //seekbar設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();

                if(!liteStatus & progress > 0) {
                    seekBar.setProgress(100);
                    liteStatus = true;
                }
                else if(liteStatus & progress < 100) {
                    seekBar.setProgress(0);
                    liteStatus = false;
                }
            }
        });


    }*/
