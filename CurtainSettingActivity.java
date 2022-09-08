package com.rexlite.rexlitebasicnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.ExpandDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurtainSettingActivity extends AppCompatActivity {
    private static final String TAG = "csetting";
    public static byte[] deviceSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    String deviceCh;
    int deviceChNum = 1;
    public static String type;
    public static byte[] btnType = new byte[1];
    int page;
    //命名相關
    String subtitleText;

    public static ListAdapter listAdapter ;
    private static RecyclerView mRecyclerView;
    private static List<Device> deviceList = new ArrayList<Device>();
    public static List<Device> adapterList = new ArrayList<Device>();
    private TextView title;

    //系統相關
    DeviceCommand deviceCommand = new DeviceCommand();
    public static ExecutorService exec = Executors.newCachedThreadPool();
    CurtainSettingActivity.MyBroadcast myBroadcast = new CurtainSettingActivity.MyBroadcast();
    public static Map<Integer, String> statusMap = new HashMap<>();
    public static boolean loading = true; //判斷是否取得資料
    View loadingAni;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (page == 1) {
            Intent intent = new Intent(CurtainSettingActivity.this, Max1SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN", deviceSN);
            bundle.putString("deviceName", deviceName);
            bundle.putString("deviceCH", deviceCh);
            bundle.putString("deviceSubtitle", subtitleText);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(CurtainSettingActivity.this);
        }
        if (page == 2) {
            Intent intent = new Intent(CurtainSettingActivity.this, Max2SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN", deviceSN);
            bundle.putString("deviceName", deviceName);
            bundle.putString("deviceCH", deviceCh);
            bundle.putString("deviceSubtitle", subtitleText);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(CurtainSettingActivity.this);
        }
        if (page == 3) {
            Intent intent = new Intent(CurtainSettingActivity.this, Max3SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN", deviceSN);
            bundle.putString("deviceName", deviceName);
            bundle.putString("deviceCH", deviceCh);
            bundle.putString("deviceSubtitle", subtitleText);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(CurtainSettingActivity.this);
        }
    }
    private static String convertStringToHex(String str) {
        StringBuilder stringBuilder = new StringBuilder();

        char[] charArray = str.toCharArray();

        for (char c : charArray) {
            String charToHex = Integer.toHexString(c);
            stringBuilder.append(charToHex);
        }

        return stringBuilder.toString();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_setting);
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button rightIcon = findViewById(R.id.right_button);
        title = findViewById(R.id.toolbar_title);
        title.setText("Control Setting");
        mRecyclerView = (RecyclerView) findViewById(R.id.radio_recyclerView);
        //取得動畫View
        loadingAni = findViewById(R.id.spin_kit);
        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        subtitleText = info.getString("deviceSubtitle", "");
        deviceSN = info.getByteArray("deviceSN");
        deviceName = info.getString("deviceName");
        deviceChNum = info.getInt("chNum", 0);
        deviceCh = info.getString("deviceCH", "0");
        type = info.getString("type");
        //初始化List避免重複增加
        adapterList = new ArrayList<>();
        if(deviceChNum == 1){
            btnType[0] =  0x44;
        }
        if(deviceChNum == 2){
            btnType[0]= 0x22;
        }
        if(deviceChNum == 3){
            btnType[0] = 0x11;
        }
        Log.d(TAG, "ch: "+deviceChNum);
        page = info.getInt("page");
        rightIcon.setVisibility(View.GONE);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (page == 1) {
                    Intent intent = new Intent(CurtainSettingActivity.this, Max1SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN", deviceSN);
                    bundle.putString("deviceName", deviceName);
                    bundle.putString("deviceCH", deviceCh);
                    bundle.putString("deviceSubtitle", subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(CurtainSettingActivity.this);
                }
                if (page == 2) {
                    Intent intent = new Intent(CurtainSettingActivity.this, Max2SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN", deviceSN);
                    bundle.putString("deviceName", deviceName);
                    bundle.putString("deviceCH", deviceCh);
                    bundle.putString("deviceSubtitle", subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(CurtainSettingActivity.this);
                }
                if (page == 3) {
                    Intent intent = new Intent(CurtainSettingActivity.this, Max3SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN", deviceSN);
                    bundle.putString("deviceName", deviceName);
                    bundle.putString("deviceCH", deviceCh);
                    bundle.putString("deviceSubtitle", subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(CurtainSettingActivity.this);
                }

            }

        });
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        byte[] cmd = deviceCommand.curtainSettingStatus(type, deviceSN);
        maxLiteCommand(cmd);
        new Thread(() -> {

            List<Device> data = DataBase.getInstance(CurtainSettingActivity.this).getDataUao().findDataByDeviceId("0b");
            deviceList = data;
            for (int i = 0; i < deviceList.size(); i++) {
                adapterList.add(deviceList.get(i));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter = new ListAdapter(adapterList, CurtainSettingActivity.this);
                    Log.d(TAG, "長度: "+adapterList.size());
                    //預設要打勾的項目
                   // listAdapter.getMap().put(1, true);
                    mRecyclerView.setAdapter(listAdapter);
                    listAdapter.setRecyclerViewOnItemClickListener(new ListAdapter.RecyclerViewOnItemClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position) {

                            listAdapter.setSelectItem(position);
                        }

                        @Override
                        public boolean onItemLongClickListener(View view, int position) {
                            return true;
                        }
                    });
                }
            });
        }).start();
      /*  adapterList.add("M'S_0011");
        adapterList.add("M'S_0012");
        adapterList.add("M'S_0013");
        adapterList.add("M'S_0014");
        adapterList.add("M'S_0015");
        adapterList.add("M'S_0016");*/


    }

    //發送指令
    public static void maxLiteCommand(byte msgCRC[]) {
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
                    int deviceNum = Integer.parseInt(hex.substring(0, 2), 16);
                    // Log.d(TAG, "onCurtainSetting: " + deviceNum);
                    if (deviceNum / 8 == (resultBytes.length) / 8) {

                        for (int i = 1; i <= (deviceNum / 8 -1); i++) {
                            byte[] device = Arrays.copyOfRange(resultBytes,i * 8 , i * 8 + 8);
                            String deviceSN = UDP.byteArrayToHexStr(device).substring(2,14);
                            String deviceType =  UDP.bytesToHex(device).substring(0,2);
                            String bindButton = UDP.byteArrayToHexStr(device).substring(14);

                            for(int j = 0 ; j < adapterList.size(); j++){
                                if(adapterList.get(j).getDeviceSN().equals(deviceSN) && adapterList.get(j).getDeviceId().equals(deviceType)){
                                    int n =  Integer.parseInt(bindButton, 16);
                                    int b = Integer.parseInt(UDP.byteArrayToHexStr(btnType),16);
                                    statusMap.put(j,bindButton);
                                    if((n&b) == b){
                                        listAdapter.getMap().put(j,true);
                                        //listAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "onReceive:V");
                                    }


                                }
                            }

                            Log.d(TAG, "裝置" + i + ": " + UDP.byteArrayToHexStr(device));
                        }
                        loading = false;
                        listAdapter.notifyDataSetChanged();
                        loadingAni.setVisibility(View.GONE);
                    }
                    /*if(Arrays.equals(resultBytes, statusMsg)){
                        Log.d(TAG, "r"+UDP.byteArrayToHexStr(resultBytes));
                        Log.d(TAG, "S: "+UDP.byteArrayToHexStr(statusMsg));
                    }*/


                    break;

            }
        }
    }

    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>
            implements View.OnClickListener {

        private List<Device> list;
        private Context context;

        private boolean isshowBox = false;

        private Map<Integer, Boolean> map = new HashMap<>();

        private RecyclerViewOnItemClickListener onItemClickListener;

        public ListAdapter(List<Device> list, Context context) {
            this.list = list;
            this.context = context;
            initMap();
        }


        private void initMap() {
            for (int i = 0; i < list.size(); i++) {
                map.put(i, false);
            }
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView title;
            private CheckBox checkBox;
            private View root;

            public ViewHolder(View root) {
                super(root);
                this.root = root;
                title = (TextView) root.findViewById(R.id.tv);
                checkBox = (CheckBox) root.findViewById(R.id.cb);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.title.setText(list.get(position).getDeviceName());

            //設置Tag
            holder.root.setTag(position);
            if(loading){
                holder.checkBox.setEnabled(false);
            } else {
                holder.checkBox.setEnabled(true);
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //用map集合保存
                        int limit = 0;
                        map.put(position, isChecked);
                        for (int i = 0; i < map.size(); i++) {
                            if (map.get(i)) {
                                limit++;
                            }
                        }
                        //限制打勾的數量
                        if (limit > 5) {
                            holder.checkBox.setChecked(false);
                            map.put(position, false);
                        }
                        if (isChecked) {
                            String bindButton = statusMap.get(position);
                            Log.d(TAG, "前: " + bindButton);
                            int n = Integer.parseInt(bindButton, 16);
                            int b = Integer.parseInt(UDP.byteArrayToHexStr(btnType), 16);
                            if (n != b) {
                                String newButton = String.format("%02X", n | b);
                                Log.d(TAG, "後: " + newButton);
                                statusMap.put(position, newButton);
                                Device device = adapterList.get(position);
                                Log.d(TAG, "sn: " + device.getDeviceSN());
                                DeviceCommand deviceCommand = new DeviceCommand();
                                byte[] cmd = deviceCommand.curtainBindingSetting(type, deviceSN, device.getDeviceId(), device.getDeviceSN(), newButton);
                                Log.d(TAG, "cmd: " + UDP.byteArrayToHexStr(cmd));
                                maxLiteCommand(cmd);


                            }

                        }
                        if (!isChecked) {
                            String bindButton = statusMap.get(position);
                            Log.d(TAG, "前: " + bindButton);
                            int n = Integer.parseInt(bindButton, 16);
                            int b = Integer.parseInt(UDP.byteArrayToHexStr(btnType), 16);
                            b = ~b;
                            String newButton = String.format("%02X", n & b);
                            Log.d(TAG, "後: " + newButton);
                            statusMap.put(position, newButton);
                            Device device = adapterList.get(position);
                            DeviceCommand deviceCommand = new DeviceCommand();
                            byte[] cmd = deviceCommand.curtainBindingSetting(type, deviceSN, device.getDeviceId(), device.getDeviceSN(), newButton);
                            Log.d(TAG, "cmd: " + UDP.byteArrayToHexStr(cmd));
                            maxLiteCommand(cmd);

                        }
                       // Log.d(TAG, "狀態: " + isChecked);
                    }
                });
            }
            if (map.get(position) == null) {
                map.put(position, false);
            }
            holder.checkBox.setChecked(map.get(position));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_item, parent, false);
            ViewHolder vh = new ViewHolder(root);
            root.setOnClickListener(this);
            return vh;
        }

        //單按事件
        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {

                onItemClickListener.onItemClickListener(v, (Integer) v.getTag());
            }
        }


        //設置點擊事件
        public void setRecyclerViewOnItemClickListener(RecyclerViewOnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }


        public void setSelectItem(int position) {

            if (map.get(position)) {
                map.put(position, false);
            } else {
                map.put(position, true);
            }
            notifyItemChanged(position);
        }

        //返回集合给MainActivity
        public Map<Integer, Boolean> getMap() {
            return map;
        }


        public interface RecyclerViewOnItemClickListener {
            //單按事件
            void onItemClickListener(View view, int position);

            //長按事件
            boolean onItemLongClickListener(View view, int position);
        }
    }

}
