package com.rexlite.rexlitebasicnew;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.button.MaterialButton;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CurtainFragment extends Fragment {

    int chNum; //代表從哪個CH開啟的
    private static final String TAG = "curtain";
    DeviceCommand deviceCommand = new DeviceCommand();
    byte[] deviceSN;
    byte[] chData;//指令(還不包括亮度與SN)
    byte maxType;//控制的裝置類型
    Handler handler = new Handler();
    Runnable runnable;
    Button upButton;
    Button downButton;
    Button settingButton;
    int currentDegree;
    String deviceName;
    String subtitleText;
    String type;
    int page;
    MaterialButton shortcutBtn;
    List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    //seekbar設定參數
    int step = 1;
    int max = 5;
    int min = 0;
    ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_curtain, container, false);
    }

    //fragment開始的方法
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //初始化設定
        Bundle bundle = getArguments();
        chNum = bundle.getInt("chNum");
        Log.d(TAG, "CH: "+chNum);
        deviceSN = bundle.getByteArray("deviceSN");
        maxType = bundle.getByte("maxType");
        type = bundle.getString("type");
        subtitleText = bundle.getString("deviceSubtitle");
        page = bundle.getInt("page");
        settingButton = view.findViewById(R.id.SettingButton);
        //初始化shortcut
        deviceName = bundle.getString("deviceName");
        new Thread(() -> {
            List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
            shortcutCheckData = ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().findDataByhostSNandTypeandCH(UDP.byteArrayToHexStr(deviceSN),  String.format("%02X ", maxType),"CH" + chNum);
            ShortcutDataBase.getInstance(getActivity()).getShortcutDataDao().updateTypeWithID(shortcutCheckData.get(0).getId(), "MAX-Drapes(angle)");
        }).start();
        Shortcut shortcut = new Shortcut();
        shortcut.setIcon(R.drawable.curtain_degree_icon);
        String deviceCH = Integer.toString(chNum);
        SeekBar seekBar = view.findViewById(R.id.curtain_seekBar);
        seekBar.setMax( (max - min) / step );
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = min + (progress * step);
                //currentDegree = value;
               // Log.d(TAG, "目前角度: "+currentDegree);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        upButton = view.findViewById(R.id.up_button);
        downButton = view.findViewById(R.id.down_button);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] chData;
                byte[] sendCMD;
                chData = deviceCommand.settingMaxLiteBtnCMD(chNum, true, maxType);
                sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                maxLiteCommand(sendCMD);
            }
        });
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] chData;
                byte[] sendCMD;
                chData = deviceCommand.settingMaxLiteBtnCMD(chNum, false, maxType);
                sendCMD = deviceCommand.settingCMD(chData, maxType, deviceSN);
                maxLiteCommand(sendCMD);
            }
        });
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CurtainSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN",deviceSN);
                bundle.putString("deviceName",deviceName);
                bundle.putString("deviceSubtitle",subtitleText);
                bundle.putInt("chNum",chNum);
                bundle.putString("deviceCH",deviceCH);
                bundle.putInt("page",page);
                // String type = String.format("%02X ", maxType);
                bundle.putString("type",type);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(getActivity());
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
                //設定計時發送調整角度的指令 才不會造成滑動UI時傳送過多指令
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        int progress = seekBar.getProgress();
                        if (currentDegree != progress) {
                            chData = deviceCommand.settingCurtainCMD(chNum,maxType);
                            byte cmd[];
                            cmd = settingCMD(chData, progress);
                            currentDegree = progress;
                            maxLiteCommand(cmd);
                        }
                        handler.postDelayed(this, 300);
                    }
                };
        runnable.run(); //執行計時器
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
    //根據調整的數字轉成相對應的指令
    private byte[] settingCMD(byte[] chData, int degree) {
        byte[] liteCHCmd = {maxType, 0x06};
        String numHex = String.format("%1$02X", degree);
        // Log.d(TAG, "目前亮度: " + percentHex);
        byte[] numCmd = UDP.hexToByte(numHex);
        byte[] CRCTemp = deviceCommand.ConcatAll(liteCHCmd, deviceSN, chData, numCmd);
        byte[] CRCNum = deviceCommand.GetCRC(CRCTemp);
        byte[] sendCMD = deviceCommand.ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }
}