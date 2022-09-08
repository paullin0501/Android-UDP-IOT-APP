package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DeviceNameSettingActivity extends AppCompatActivity {
    private static final String TAG = "editName";
    private RecyclerView mRecyclerView;
    private TextView title;
    private TextView radioTitle;
    private EditText editText;
    private String subtitleText;
    RadioButton radioButton;
    boolean btnCheck = false;
    private int page;//記錄從哪個頁面進入
    private String deviceTye;
    byte[] deviceSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    String deviceCh;
    StringAdapter stringAdapter;
    private String selectedRoom;
    public int mSelectedItem = -1;
    //更新相關
    final UpdateDialog loadingDialog = new UpdateDialog(DeviceNameSettingActivity.this);
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
        setContentView(R.layout.activity_device_name_setting);
        mRecyclerView = (RecyclerView) findViewById(R.id.radio_recyclerView);
        title = findViewById(R.id.toolbar_title);
        radioTitle = findViewById(R.id.radio_title);
        editText = findViewById(R.id.edit_text);
        radioButton = findViewById(R.id.custom_radio_btn);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if(!btnCheck){
                        mSelectedItem = -1;
                        btnCheck = true;
                        stringAdapter.notifyDataSetChanged();
                        selectedRoom = editText.getText().toString();
                        radioButton.setChecked(true);
                        Log.d(TAG, "customRoom: "+selectedRoom);
                    }
                } else {

                }
            }
        });

        //自訂名稱按鈕事件
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!btnCheck){
                    mSelectedItem = -1;
                    btnCheck = true;
                    stringAdapter.notifyDataSetChanged();
                    selectedRoom = editText.getText().toString();
                    Log.d(TAG, "customRoom: "+selectedRoom);
                }
            }
        });
        title.setText("Edit Name");
        ImageView leftIcon = findViewById(R.id.left_icon);
        Button rightIcon = findViewById(R.id.right_button);
        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        page = info.getInt("page", 0);
        deviceTye = info.getString("type");
        deviceSN = info.getByteArray("deviceSN");
        deviceName = info.getString("deviceName");
        deviceCh = info.getString("deviceCH","0");
        subtitleText = info.getString("deviceSubtitle","");
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (page == 1) {
                    Intent intent = new Intent(DeviceNameSettingActivity.this, Max1SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",deviceName);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
                }
                if (page == 2) {
                    Intent intent = new Intent(DeviceNameSettingActivity.this, Max2SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",deviceName);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
                }
                if (page == 3) {
                    Intent intent = new Intent(DeviceNameSettingActivity.this, Max3SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("deviceSN",deviceSN);
                    bundle.putString("deviceName",deviceName);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
                }
                if (page == 4) {
                    Intent intent = new Intent(DeviceNameSettingActivity.this, SceneSettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",deviceSN);
                    bundle.putString("deviceName",deviceName);
                    bundle.putString("deviceCH",deviceCh);
                    bundle.putString("deviceSubtitle",subtitleText);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
                }
                if (page == 5) {
                    Intent intent = new Intent(DeviceNameSettingActivity.this, ListDeviceActivity.class);
                    startActivity(intent);
                    Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
                }
            }
        });

        rightIcon.setText("Next");
        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceNameSettingActivity.this, DeviceNameSettingSubtitleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("deviceSN",deviceSN);
                bundle.putString("deviceName",deviceName);
                bundle.putString("deviceCH",deviceCh);
                bundle.putString("type",deviceTye);
                bundle.putInt("page",page);

                if(selectedRoom==null && editText.getText().toString().equals("")){
                    Toast.makeText(DeviceNameSettingActivity.this, "please enter or select a name", Toast.LENGTH_SHORT).show();
                } else {
                    if (mSelectedItem == -1) {
                        bundle.putString("selectedRoom", editText.getText().toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Animatoo.animateSlideLeft(DeviceNameSettingActivity.this);
                    } else {
                        bundle.putString("selectedRoom", selectedRoom);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Animatoo.animateSlideLeft(DeviceNameSettingActivity.this);
                    }
                }


            }
        });
        //設定字體為粗體
        radioTitle.setTypeface(null, Typeface.BOLD);

        //將list加入名稱
        ArrayList<String> rooms = new ArrayList<String>();

        rooms.add("Living Room");
        rooms.add("Master Bedroom");
        rooms.add("Bed Room");
        rooms.add("Kids Room");
        rooms.add("Study Room");
        rooms.add("Guest Room");
        rooms.add("Kitchen");
        rooms.add("Dining Room");
        rooms.add("Ball Room");
        rooms.add("Staircase");
        rooms.add("Balcony");
        rooms.add("Office");
        rooms.add("Meeting Room");
        rooms.add("Dressing Room");
        rooms.add("Garage");
        rooms.add("Loft");
        rooms.add("Garden");
        rooms.add("Leisure Room");
        rooms.add("Front Desk");
        rooms.add("Parking Lane");
        rooms.add("Studio");
        rooms.add("Factory");
        rooms.add("Locker");
        rooms.add("Lobby");
        rooms.add("Walk-in Closet");
        rooms.add("Shower Room");
        rooms.add("Maid Room");
        rooms.add("Walkway");
        rooms.add("Co-working Area");
        rooms.add("Personal Cubic");
        rooms.add("Tea Room");
        rooms.add("Staff Room");
        stringAdapter= new StringAdapter(this,rooms);
        mRecyclerView.setAdapter(stringAdapter);


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (page == 1) {
            Intent intent = new Intent(DeviceNameSettingActivity.this, Max1SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN",deviceSN);
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceCH",deviceCh);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
        }
        if (page == 2) {
            Intent intent = new Intent(DeviceNameSettingActivity.this, Max2SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN",deviceSN);
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceCH",deviceCh);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
        }
        if (page == 3) {
            Intent intent = new Intent(DeviceNameSettingActivity.this, Max3SettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("deviceSN",deviceSN);
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceCH",deviceCh);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
        }
        if (page == 4) {
            Intent intent = new Intent(DeviceNameSettingActivity.this, SceneSettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putByteArray("maxSceneSN",deviceSN);
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceCH",deviceCh);
            intent.putExtras(bundle);
            startActivity(intent);
            Animatoo.animateSlideRight(DeviceNameSettingActivity.this);
        }

    }

    public abstract class RadioAdapter<T> extends RecyclerView.Adapter<RadioAdapter.ViewHolder> {

        public List<T> mItems;
        private Context mContext;
        boolean firstTime = true; //判斷是否為進入畫面的時候
        public RadioAdapter(Context context, List<T> items) {
            mContext = context;
            mItems = items;
        }

        @Override
        public void onBindViewHolder(RadioAdapter.ViewHolder viewHolder, final int i) {


            viewHolder.mRadio.setChecked(i == mSelectedItem);
            //當有預設名字時 代入選擇的項目
            if(mItems.get(i).equals(deviceName) && firstTime){
                viewHolder.mRadio.setChecked(true);
                firstTime = false;
            }
            //當名稱是自訂時代入自訂的名稱
            if(!mItems.get(i).equals(deviceName) && firstTime && !deviceName.contains("M'S") && !deviceName.contains("M'L"))
            {
               radioButton.setChecked(true);
               editText.setText(deviceName);
                firstTime = false;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            final View view = inflater.inflate(R.layout.radio_item, viewGroup, false);
            return new ViewHolder(view);
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public RadioButton mRadio;
            public TextView mText;

            public ViewHolder(final View inflate) {
                super(inflate);
                mText = (TextView) inflate.findViewById(R.id.radio_text);
                mRadio = (RadioButton) inflate.findViewById(R.id.radio_btn);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedItem = getAdapterPosition();
                        selectedRoom = mText.getText().toString();

                        radioButton.setChecked(false);
                        btnCheck = false;
                        Log.d(TAG, "room name: " + selectedRoom);
                        notifyDataSetChanged();
                    }
                };
                itemView.setOnClickListener(clickListener);

                mRadio.setOnClickListener(clickListener);
            }
        }
    }

    public class StringAdapter extends RadioAdapter<String> {
        public StringAdapter(Context context, List<String> items) {
            super(context, items);
        }

        @Override
        public void onBindViewHolder(RadioAdapter.ViewHolder viewHolder, int i) {
            super.onBindViewHolder(viewHolder, i);
            viewHolder.mText.setText(mItems.get(i));

        }
    }

}