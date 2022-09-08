package com.rexlite.rexlitebasicnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.daimajia.swipe.SwipeLayout;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.ExpandDevice;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;

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

public class ListDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ListDevice";
    private ExpandableListView expandableListView;
    private ListExpandableViewAdapter expandableListViewAdapter;
    private List<String> listDataGroup;
    private HashMap<String,List<ExpandDevice>> listDataChild;
    private List<ExpandDevice> expandDeviceMax1 = new ArrayList<>();
    private List<ExpandDevice> expandDeviceMax2 = new ArrayList<>();
    private List<ExpandDevice> expandDeviceMax3 = new ArrayList<>();
    private List<ExpandDevice> expandDeviceScene= new ArrayList<>();
    List<Device> max1List = new ArrayList<>();
    List<Device> max2List = new ArrayList<>();
    List<Device> max3List = new ArrayList<>();
    List<Device> maxSceneList = new ArrayList<>();
    DeviceCommand deviceCommand = new DeviceCommand();
    public String responseSN;  //目前回傳的SN
    public String responseType; //目前回傳的裝置種類
    private String responseShine; //目前該裝置是否閃爍

    //網路連線相關
     ListDeviceActivity.MyBroadcast myBroadcast = new ListDeviceActivity.MyBroadcast();
    ExecutorService exec = Executors.newCachedThreadPool();
    boolean isUsed = false; //如果在操作中就不更新狀態避免跳動
    Runnable runnable; //計時器
    Handler handler = new Handler(); //計時器
    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(ListDeviceActivity.this);
    private boolean finishUpdate = false;
    @Override
    public void onStart() {
        super.onStart();
        // 在此Activity啟用EventBus
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device);
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button rightIcon = findViewById(R.id.right_button);
        TextView title = findViewById(R.id.toolbar_title);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListDeviceActivity.this, MainActivity.class);
                startActivity(intent);
                Animatoo.animateSlideRight(ListDeviceActivity.this);
            }
        });
        rightIcon.setVisibility(View.GONE);

        title.setText("List device ID");
        // initializing the views
        initViews();
        // initializing the listeners
        initListeners();
        // initializing the objects
        initObjects();
        // preparing list data
         initListData();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(childPosition > 0) {
                    Device device = listDataChild.get(listDataGroup.get(groupPosition))
                            .get(childPosition - 1).getDevice();
                    Intent intent = new Intent(ListDeviceActivity.this, DeviceNameSettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN", UDP.hexToByte(device.getDeviceSN()));
                    bundle.putString("deviceName", device.getDeviceName());
                    bundle.putString("deviceCH", "0");
                    bundle.putInt("page", 5);
                    bundle.putString("type", device.getDeviceId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(ListDeviceActivity.this);
                }
                return true;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = expandableListView.getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                if(childPosition > 0) {
                    Device device = listDataChild.get(listDataGroup.get(groupPosition))
                            .get(childPosition - 1).getDevice();
                    new AlertDialog.Builder(ListDeviceActivity.this)
                            .setMessage("It will remove the related shortcuts as well.")
                            .setTitle("Would you like to remove " + listDataChild.get(listDataGroup.get(groupPosition)).get(childPosition - 1).getDevice().getDeviceName() + " from App?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //listDataChild.remove(listDataGroup.get(position));
                                    Log.d(TAG, "onClick: " + device.getDeviceId().toUpperCase().equals("0B"));
                                    listDataChild.get(listDataGroup.get(groupPosition)).remove(childPosition - 1);

                                    expandableListViewAdapter.notifyDataSetChanged();
                                    new Thread(() -> {
                                        DataBase.getInstance(ListDeviceActivity.this).getDataUao().deleteDevice(device.getDeviceId(),device.getDeviceSN());
                                        String type = null;
                                        //兩個資料庫型態儲存的格式不同需要做轉換
                                        if(device.getDeviceId().equals("0b")){
                                            type = String.format("%02X ", 0x0b);
                                        }
                                        if(device.getDeviceId().equals("14")){
                                            type = String.format("%02X ", 0x14);
                                        }
                                        if(device.getDeviceId().equals("16")){
                                            type = String.format("%02X ", 0x16);
                                        }
                                        if(device.getDeviceId().equals("18")){
                                            type = String.format("%02X ", 0x18);
                                        }
                                        Log.d(TAG, "type: "+type);
                                        ShortcutDataBase.getInstance(ListDeviceActivity.this).getShortcutDataDao().deleteDeviceShortcut(type,device.getDeviceSN());
                                    }).start();

                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return true;
            }
        });
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        //設定計時設定是否處在使用者操作的狀態
        runnable = new Runnable() {
            @Override
            public void run() {
                isUsed = false;
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
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
        expandableListViewAdapter = new ListExpandableViewAdapter(this, listDataGroup, listDataChild);
        // setting list adapter
        //只留child的分隔線
        expandableListView.setDivider(null);
        expandableListView.setDividerHeight(10);

        expandableListView.setAdapter(expandableListViewAdapter);
        //設定item點擊事件監聽
       /* expandableListView.setOnChildClickListener((expandableListView,view,parentPos,childPos,l)->{
            if(childPos > 0) {
                Device device = listDataChild.get(this.listDataGroup.get(parentPos))
                        .get(childPos - 1).getDevice();
                Intent intent = new Intent(ListDeviceActivity.this, DeviceNameSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN", UDP.hexToByte(device.getDeviceSN()));
                bundle.putString("deviceName", device.getDeviceName());
                bundle.putString("deviceCH", "0");
                bundle.putInt("page", 5);
                bundle.putString("type", device.getDeviceId());
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideLeft(ListDeviceActivity.this);
            }
            return true;
        });*/


    }

    private void initListData() {
        // Adding group data
        // loadingDialog.dismissDialog();
        listDataGroup.add("MAXLiTE1");
        listDataGroup.add("MAXLiTE2");
        listDataGroup.add("MAXLiTE3");
        listDataGroup.add("MAXScene");
        // array of strings
        String[] array;
        new Thread(() -> {
            List<Device> data = DataBase.getInstance(ListDeviceActivity.this).getDataUao().findDataByDeviceId("14");
            max1List = data;
            List<Device> data1 = DataBase.getInstance(ListDeviceActivity.this).getDataUao().findDataByDeviceId("16");
            max2List = data1;
            List<Device> data2 = DataBase.getInstance(ListDeviceActivity.this).getDataUao().findDataByDeviceId("18");
            max3List = data2;
            List<Device> data3 = DataBase.getInstance(ListDeviceActivity.this).getDataUao().findDataByDeviceId("0b");
            maxSceneList = data3;
            // Adding child data
            for( Device device:max1List){
                ExpandDevice expandDevice = new ExpandDevice(false,device);
                expandDeviceMax1.add(expandDevice);
            }
            for( Device device:max2List){
                ExpandDevice expandDevice = new ExpandDevice(false,device);
                expandDeviceMax2.add(expandDevice);
            }
            for( Device device:max3List){
                ExpandDevice expandDevice = new ExpandDevice(false,device);
                expandDeviceMax3.add(expandDevice);
            }
            for( Device device:maxSceneList){
                ExpandDevice expandDevice = new ExpandDevice(false,device);
                expandDeviceScene.add(expandDevice);
            }
           /* expandChildrenMax1.setDeviceList(data);
            expandChildrenMax2.setDeviceList(data1);
            expandChildrenMax3.setDeviceList(data2);
            expandChildrenScene.setDeviceList(data3);*/
           // Log.d(TAG, "initListData: "+max1List);
            listDataChild.put(listDataGroup.get(0), expandDeviceMax1);
            listDataChild.put(listDataGroup.get(1), expandDeviceMax2);
            listDataChild.put(listDataGroup.get(2), expandDeviceMax3);
            listDataChild.put(listDataGroup.get(3), expandDeviceScene);

        }).start();

        expandableListViewAdapter.notifyDataSetChanged();
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
                    Log.d(TAG, "onListReceive: " + hex);
                    /*if(Arrays.equals(resultBytes, statusMsg)){
                        Log.d(TAG, "r"+UDP.byteArrayToHexStr(resultBytes));
                        Log.d(TAG, "S: "+UDP.byteArrayToHexStr(statusMsg));
                    }*/
                    //判斷當前裝置的閃爍狀態
                    if (resultBytes.length == 25) {
                        responseSN = hex.substring(32, 44);
                        responseType = hex.substring(30,32);
                        responseShine = hex.substring(24,26);
                        /*Log.d(TAG, "sn: "+responseSN);
                        Log.d(TAG, "type: "+responseType);
                        Log.d(TAG, "shine: "+responseShine);*/
                        if(!isUsed) {
                            if (responseType.equals("14")) {
                                for (ExpandDevice expandDevice : expandDeviceMax1) {
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("01")) {
                                        expandDevice.setShine(true);
                                    }
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("00")) {
                                        expandDevice.setShine(false);
                                    }
                                }
                                listDataChild.replace(listDataGroup.get(0), expandDeviceMax1);
                                expandableListViewAdapter.notifyDataSetChanged();
                            }
                            if (responseType.equals("16")) {
                                for (ExpandDevice expandDevice : expandDeviceMax2) {
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("01")) {
                                        expandDevice.setShine(true);
                                    }
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("00")) {
                                        expandDevice.setShine(false);
                                    }
                                }
                                listDataChild.replace(listDataGroup.get(1), expandDeviceMax2);
                                expandableListViewAdapter.notifyDataSetChanged();
                            }
                            if (responseType.equals("18")) {
                                for (ExpandDevice expandDevice : expandDeviceMax3) {
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("01")) {
                                        expandDevice.setShine(true);
                                    }
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("00")) {
                                        expandDevice.setShine(false);
                                    }
                                }
                                listDataChild.replace(listDataGroup.get(2), expandDeviceMax3);
                                expandableListViewAdapter.notifyDataSetChanged();
                            }
                            if (responseType.equals("0B")) {
                                for (ExpandDevice expandDevice : expandDeviceScene) {
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("01")) {
                                        expandDevice.setShine(true);
                                    }
                                    if (expandDevice.getDevice().getDeviceSN().equals(responseSN) && responseShine.equals("00")) {
                                        expandDevice.setShine(false);
                                    }
                                }
                                listDataChild.replace(listDataGroup.get(3), expandDeviceScene);
                                expandableListViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    break;

            }
        }
    }
    public interface OnAddDeviceListener {


        //單按事件
        void OnChildClick(int groupPosition, int childPosition);
        //側滑刪除事件
        void OnChildDel(int groupPosition, int childPosition);
        //長按事件
        void OnChildLongClick(int groupPosition, int childPosition);


    }


    public void setOnAddDeviceListener(OnAddDeviceListener l) {
        this.listener = l;

    }

    private OnAddDeviceListener listener;
    public  class ListExpandableViewAdapter extends BaseExpandableListAdapter {
        private Context context;
        // group titles
        private List<String> listDataGroup;
        // child data
        private HashMap<String, List<ExpandDevice>> listDataChild;
        private static final String TAG = "expandList";
        private SwipeLayout currentExpandedSwipeLayout;
        public ListExpandableViewAdapter(Context context, List<String> listDataGroup,
                                         HashMap<String, List<ExpandDevice>>listChildData) {
            this.context = context;
            this.listDataGroup = listDataGroup;
            this.listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                    .get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //the first row is used as header
            if (childPosition == 0) {
                //留一個子資料的位置當標題
                convertView = layoutInflater.inflate(R.layout.expandable_header, null);
            }
            if (childPosition>0 ) {
                //取得子項目資料
                ExpandDevice device = (ExpandDevice) getChild(groupPosition, childPosition-1);
                convertView = layoutInflater.inflate(R.layout.device_list_search_row_child, null,false);



                // ImageView childImg = (ImageView) convertView.findViewById(R.id.search_icon);
                CheckBox childImg = (CheckBox)  convertView.findViewById(R.id.search_icon);
                TextView textViewChild = convertView.findViewById(R.id.nameTextChild);
                TextView subtitleTextViewChild = convertView.findViewById(R.id.subtitleTextChild);
                TextView idTextView = convertView.findViewById(R.id.idTextView);
             //   SwipeLayout swipeLayout = convertView.findViewById(R.id.swipe);
                //滑動
              /*  if(convertView == null) {

                    swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
                    swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewWithTag("Bottom2"));
                }*/
                /*swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                    @Override
                    public void onStartOpen(SwipeLayout layout) {
                        if (currentExpandedSwipeLayout != null&&currentExpandedSwipeLayout!=layout)
                            currentExpandedSwipeLayout.close(true);

                    }

                    @Override
                    public void onOpen(SwipeLayout layout) {
                        currentExpandedSwipeLayout = layout;


                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {

                    }

                    @Override
                    public void onClose(SwipeLayout layout) {
                    }

                    @Override
                    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                    }

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                    }
                });*/
                ObjectAnimator a = ObjectAnimator.ofInt(textViewChild, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
                ObjectAnimator a1 = ObjectAnimator.ofInt(subtitleTextViewChild, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
                ObjectAnimator a2 = ObjectAnimator.ofInt(idTextView, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
                AnimatorSet t = new AnimatorSet();
                AnimatorSet t1 = new AnimatorSet();
                AnimatorSet t2 = new AnimatorSet();
                boolean isShine = listDataChild.get(listDataGroup.get(groupPosition))
                        .get(childPosition-1).isShine();
                Log.d(TAG, "getChildView: isShine"+isShine);

                //增加checkbox的點擊範圍
                final View p = (View) childImg.getParent();
                p.post( new Runnable() {
                    // Post in the parent's message queue to make sure the parent
                    // lays out its children before we call getHitRect()
                    public void run() {
                        final Rect r = new Rect();
                        childImg.getHitRect(r);
                        r.top -= 4;
                        r.bottom += 4;
                        parent.setTouchDelegate( new TouchDelegate( r , childImg));
                    }
                });

                //播放動畫邏輯
                if(isShine){
                    animateText(textViewChild,true,a,t);
                    animateText(subtitleTextViewChild,true,a1,t1);
                    animateText(idTextView,true,a2,t2);
                }
                if(!isShine){
                    animateText(textViewChild,false,a,t);
                    animateText(subtitleTextViewChild,false,a1,t1);
                    animateText(idTextView,false,a2,t2);
                }
                childImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(childImg.isChecked()) {
                       /* animateText(textViewChild);
                        animateText(subtitleTextViewChild);
                        animateText(idTextView);*/
                            animateText(textViewChild,childImg.isChecked(),a,t);
                            animateText(subtitleTextViewChild,childImg.isChecked(),a1,t1);
                            animateText(idTextView,childImg.isChecked(),a2,t2);
                            listDataChild.get(listDataGroup.get(groupPosition))
                                    .get(childPosition-1).setShine(true);
                            byte[] sendCMD;

                            sendCMD = deviceCommand.settingSearchDeviceCMD(UDP.hexToByte(device.getDevice().getDeviceId()),true,UDP.hexToByte(device.getDevice().getDeviceSN()));
                            maxLiteCommand(sendCMD);
                            isUsed = true;
                        }
                        if(!childImg.isChecked()){
                            Log.d(TAG, "onClick: "+false);
                            animateText(textViewChild,false,a,t);
                            animateText(subtitleTextViewChild,false,a1,t1);
                            animateText(idTextView,false,a2,t2);
                            listDataChild.get(listDataGroup.get(groupPosition))
                                    .get(childPosition-1).setShine(false);
                            byte[] sendCMD;
                            sendCMD = deviceCommand.settingSearchDeviceCMD(UDP.hexToByte(device.getDevice().getDeviceId()),false,UDP.hexToByte(device.getDevice().getDeviceSN()));
                            maxLiteCommand(sendCMD);
                            isUsed = true;
                        }
                    }
                });

                textViewChild.setText(device.getDevice().getDeviceName());
                if (device.getDevice().getSubtitle() != null) {
                    subtitleTextViewChild.setText(device.getDevice().getSubtitle());
                } else {
                    subtitleTextViewChild.setVisibility(View.GONE);
                }
                idTextView.setText(device.getDevice().getDeviceId() + device.getDevice().getDeviceSN());
            }
            return convertView;

        }

        @SuppressLint("RestrictedApi")
        public void animateText(TextView textViewChild,boolean isShine, ObjectAnimator a,AnimatorSet t ){

            a.setInterpolator(new LinearInterpolator());
            a.setDuration(2000);
            a.setRepeatCount(ValueAnimator.INFINITE);
            a.setRepeatMode(ValueAnimator.REVERSE);
            a.setEvaluator(new ArgbEvaluator());

            if(isShine) {
                t.play(a);
                t.start();
            }
            if(!isShine){
                // t.removeAllListeners();
                t.end();
                t.cancel();
                textViewChild.setTextColor(Color.parseColor("#707070"));
            }
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                    .size()+1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listDataGroup.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.listDataGroup.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.device_list_row_group, null);
            }
            TextView textViewGroup = convertView
                    .findViewById(R.id.textViewGroup);
            textViewGroup.setTypeface(null, Typeface.BOLD);
            textViewGroup.setText(headerTitle);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

}