package com.rexlite.rexlitebasicnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FirmwareUpdateActivity extends AppCompatActivity {
    private static final String TAG = "firmware";
    private String systemMAC;
    private String systemVersion;
    private RecyclerView infoRecyclerView;
    private static InfoAdapter infoAdapter;
    private static UpdateDialog updateDialog;
    private static LoadingDialog loadingDialog;
    private int countApi = 0; //計算呼叫幾次api
    private static boolean updating = false; //判斷是否在更新
    //MQTT
    private Handler mHandler;
    // public  MqttManager m_mqttManager;
    public static MqttManager m_mqttManager;
    private String updateJSONData;
    private List<HttpResponse> responseData = new ArrayList<HttpResponse>(); //更新版本資訊

    private ArrayList<UpdateResponse> updateInfoList = new ArrayList<>(); //系統裝置版本資訊

    //更新相關
    final UpdateDialog updateDialog1 = new UpdateDialog(FirmwareUpdateActivity.this);
    private boolean finishUpdate = false;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FirmwareUpdateActivity.this, UserSettingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("systemMAC", systemMAC);
        bundle.putString("systemVersion", systemVersion);
        intent.putExtras(bundle);
        startActivity(intent);
        Animatoo.animateSlideRight(FirmwareUpdateActivity.this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateStatus event) {
        Log.d(TAG, "onEvent: " + event.getUpgrade());
        if(!updating) {
            if (event.getUpgrade().equals("0") && finishUpdate) {
                updateDialog1.dismissDialog();
                finishUpdate = false;
            }
            if (event.getUpgrade().equals("1")) {
                updateDialog1.startLoadingDialog();
                updateDialog1.setLoadingText("Firmware is updating...");
                finishUpdate = true;
            }
        }
        //根據mqtt的回傳值來顯示更新的UI


    }
    @Override
    public void onStop() {
        super.onStop();
        // 在Activity停用EventBus，讓Subscribe停止接收
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 在此Activity啟用EventBus
        EventBus.getDefault().register(this);
    }

    // 註冊Subscribe
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event) {
        Log.d(TAG, "onMessageEvent: " + event);
        //根據mqtt的回傳值來顯示更新的UI
        if(updating) {
            updateDialog.setLoadingText(event);
            if (event.equals("FAIL") || event.equals("FINISH")) {
                if (event.equals("FAIL")) {
                    updateDialog.dismissDialog();
                    AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareUpdateActivity.this);
                    builder.setMessage("System update fail, please contact to customer service or try again.")
                            .setCancelable(true)
                            .setTitle("Error")
                            .setPositiveButton("Ok", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    updating = false;
                } else {
                    updateDialog.dismissDialog();
                    updating = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareUpdateActivity.this);
                    builder.setMessage("System update Success.")
                            .setCancelable(true)
                            .setTitle("Success")
                            .setPositiveButton("Ok", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateJSONData = MqttManager.jsonString;
                            Log.d(TAG, "更新資訊: " + MqttManager.jsonString);
                            try {
                                if (updateJSONData != null) {
                                    JSONObject jsonObject = new JSONObject(updateJSONData);
                                    // JSONArray array = new JSONArray(updateJSONData);
                                    Log.d(TAG, "物件: " + jsonObject);
                                    //  Log.d(TAG, "物件1: "+array.get(0));
                                    Iterator it = jsonObject.keys();
                                    while (it.hasNext()) {
                                        String v = it.next().toString();
                                        Log.d(TAG, "裝置: " + v);
                                        Log.d(TAG, "版本: " + jsonObject.get(v));
                                        UpdateResponse updateResponse = new UpdateResponse();
                                        updateResponse.setVersion("Current version " + String.valueOf(jsonObject.get(v)));
                                        updateResponse.setDeviceType(v);
                                        updateInfoList.add(updateResponse);
                                        HttpResponse h = new HttpResponse();
                                        responseData.add(h);
                                        //將一筆裝置轉為json 字串 並提出post api
                                        JSONObject jo = new JSONObject();
                                        jo.put("device", v);
                                        jo.put("version", jsonObject.get(v));
                                        initPost(jo.toString(), v);
                                    }
                                    Log.d(TAG, "個數: " + updateInfoList.size());
                                    infoAdapter.notifyDataSetChanged();

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 600);
                }
            }
        }
    }

    /*   public void handleMQTT(){

       }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        infoRecyclerView = findViewById(R.id.info_recycler);
        //取得裝置資料
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        systemMAC = info.getString("systemMAC", "");
        systemVersion = info.getString("systemVersion", "");
        Log.d(TAG, "MAC: " + systemMAC);
        //更新畫面相關
        updateDialog = new UpdateDialog(FirmwareUpdateActivity.this);
        loadingDialog = new LoadingDialog(FirmwareUpdateActivity.this);
        loadingDialog.startLoadingDialog("Checking...");
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        }, 5000);

        /*//註冊 必須有@Subscriber 訂閱者
        EventBus.getDefault().register(this);*/
      //  EventBus.getDefault().register(this);
        //MQTT訂閱資訊

        m_mqttManager = MqttManager.getInstance(FirmwareUpdateActivity.this);
        Log.d(TAG, "活著嗎: "+m_mqttManager.isConnect());
        MqttManager.progress_topic = "max-system/V4/" + systemMAC + "/progress";
        MqttManager.status_topic = "max-system/V4/" + systemMAC + "/status";
        MqttManager.upgrade_topic = "max-system/V4/" + systemMAC + "/upgrade";
        MqttManager.version_TOPIC = "max-system/V4/" + systemMAC + "/version";
        MqttManager.update_topic = "max-system/V4/" + systemMAC + "/update";
        //m_mqttManager.connect("android", mHandler);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateJSONData = MqttManager.jsonString;

                Log.d(TAG, "更新資訊: " + MqttManager.jsonString);
                try {
                    if (updateJSONData != null) {
                        JSONObject jsonObject = new JSONObject(updateJSONData);
                        // JSONArray array = new JSONArray(updateJSONData);
                        Log.d(TAG, "物件: " + jsonObject);
                        //  Log.d(TAG, "物件1: "+array.get(0));
                        Iterator it = jsonObject.keys();
                        while (it.hasNext()) {
                            String v = it.next().toString();
                            Log.d(TAG, "裝置: " + v);
                            Log.d(TAG, "版本: " + jsonObject.get(v));
                            UpdateResponse updateResponse = new UpdateResponse();
                            updateResponse.setVersion("Current version " + String.valueOf(jsonObject.get(v)));
                            updateResponse.setDeviceType(v);
                            updateInfoList.add(updateResponse);
                            HttpResponse h = new HttpResponse();
                            responseData.add(h);
                            //將一筆裝置轉為json 字串 並提出post api
                            JSONObject jo = new JSONObject();
                            jo.put("device", v);
                            jo.put("version", jsonObject.get(v));
                            initPost(jo.toString(), v);
                        }
                        Log.d(TAG, "個數: " + updateInfoList.size());
                        infoAdapter.notifyDataSetChanged();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
        infoAdapter = new InfoAdapter(this);


        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        int space = 20;
        infoRecyclerView.addItemDecoration(new SpacesItemDecoration(space));
        infoRecyclerView.setLayoutManager(linearLayoutManager);
        infoRecyclerView.setAdapter(infoAdapter);

        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Firmware Update");
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirmwareUpdateActivity.this, UserSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("systemMAC", systemMAC);
                bundle.putString("systemVersion", systemVersion);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideRight(FirmwareUpdateActivity.this);
            }
        });
    }

    //因為回傳的順序會混亂 所以帶入傳的裝置名稱
    private void initPost(String json, String deviceType) {
        String url = "https://max-system.japhne.com/api/firmware/check_update/V4"; //加https才是安全連線
        Okhttp.postNetokhttp().callNet(url, json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 發送api失敗");
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
               /* try {
                    JSONObject jsonObject = new JSONObject(res);
                    Iterator it = jsonObject.keys();
                    while (it.hasNext()) {
                        HttpResponse httpResponse = new HttpResponse();
                        httpResponse.setChangeLog("123");
                        httpResponse.setDevice(String.valueOf(jsonObject.get("device")));
                        httpResponse.setChecksum(String.valueOf(jsonObject.get("checksum")));
                        httpResponse.setFirmware(String.valueOf(jsonObject.get("firmware")));
                        httpResponse.setRelease(String.valueOf(jsonObject.get("release")));
                        httpResponse.setVersion(String.valueOf(jsonObject.get("version")));
                        responseData.add(httpResponse);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                Gson gson = new Gson();
                HttpResponse httpResponse = gson.fromJson(res, HttpResponse.class);
                Log.d(TAG, "onResponse: " + httpResponse.getFirmware());
                countApi++;
                //將responseData 排序和updateInfoList一樣
                if (deviceType.equals(httpResponse.getDevice())) {
                    for (int i = 0; i < updateInfoList.size(); i++) {
                        if (updateInfoList.get(i).getDeviceType().equals(deviceType)) {
                            responseData.set(i, httpResponse);
                        }
                    }
                }
                if (httpResponse.getDevice() == null) {
                    HttpResponse h = new HttpResponse();
                    h.setDevice(deviceType);
                    for (int i = 0; i < updateInfoList.size(); i++) {
                        if (updateInfoList.get(i).getDeviceType().equals(deviceType)) {
                            responseData.set(i, h);
                        }
                    }
                }

                if (responseData.size() == 5) {
                    for (int i = 0; i < responseData.size(); i++) {
                        //   Log.d(TAG, "onResponse: " + responseData.get(i).getDevice());
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        infoAdapter.notifyDataSetChanged();
                        loadingDialog.dismissDialog();
                        Log.d(TAG, "通知: ");

                        // infoAdapter.notifyItemChanged(countApi);
                    }
                });

            }
        });
    }

    public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.Viewholder> {

        private Context context;

        // Constructor
        public InfoAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public InfoAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // to inflate the layout for each item of recycler view.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.firmware_card, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InfoAdapter.Viewholder holder, int position) {
            // to set data to textview and imageview of each card layout
            //當前系統資訊
            UpdateResponse updateResponse = updateInfoList.get(position);
            holder.deviceTitle.setText(updateResponse.getDeviceType());
            holder.currentVersion.setText(updateResponse.getVersion());
            //從api取得的資訊
            HttpResponse httpResponse = responseData.get(position);
            holder.updateDate.setText(httpResponse.getRelease());
            holder.updateVersion.setText("Version " + httpResponse.getVersion());
            holder.updateDetail.setText(httpResponse.getChangeLog());
            if (responseData.get(position).getRelease() == null) {
                holder.updateDate.setVisibility(View.GONE);
                holder.updateVersion.setVisibility(View.GONE);
                holder.updateDetail.setVisibility(View.GONE);
                holder.divideLine.setVisibility(View.GONE);
                holder.updateBtn.setVisibility(View.GONE);
                holder.latestBtn.setVisibility(View.VISIBLE);

            } else {
                holder.updateDate.setVisibility(View.VISIBLE);
                holder.updateVersion.setVisibility(View.VISIBLE);
                holder.updateDetail.setVisibility(View.VISIBLE);
                holder.divideLine.setVisibility(View.VISIBLE);
                holder.updateBtn.setVisibility(View.VISIBLE);
                holder.latestBtn.setVisibility(View.GONE);
            }
            holder.updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareUpdateActivity.this);
                    builder.setMessage("The app can not be used when the system is being updated.")
                            .setCancelable(true)
                            .setTitle("Warning")
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    updateDialog.startLoadingDialog();
                                    updating = true;
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("device", httpResponse.getDevice());
                                        jsonObject.put("version", httpResponse.getVersion());
                                        jsonObject.put("firmware", httpResponse.getFirmware());
                                        jsonObject.put("checksum", httpResponse.getChecksum());
                                        Log.d(TAG, "which string: " + jsonObject.toString());
                                        Log.d(TAG, "活著嗎: "+m_mqttManager.isConnect());
                                        m_mqttManager.publish(jsonObject.toString(), "max-system/V4/" + systemMAC + "/which");


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setNegativeButton("Cancel", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                }
            });


        }

        @Override
        public int getItemCount() {
            // this method is used for showing number
            // of card items in recycler view.
            return updateInfoList.size();
        }

        // View holder class for initializing of
        // your views such as TextView and Imageview.
        public class Viewholder extends RecyclerView.ViewHolder {

            private TextView deviceTitle, currentVersion, updateVersion, updateDate, updateDetail;
            private Button updateBtn, latestBtn;
            private View divideLine;

            public Viewholder(@NonNull View itemView) {
                super(itemView);
                deviceTitle = itemView.findViewById(R.id.type_title);
                currentVersion = itemView.findViewById(R.id.current_version_text);
                updateVersion = itemView.findViewById(R.id.update_version);
                updateDate = itemView.findViewById(R.id.release_date);
                updateDetail = itemView.findViewById(R.id.info_text);
                updateBtn = itemView.findViewById(R.id.save_button);
                latestBtn = itemView.findViewById(R.id.latest_button);
                divideLine = itemView.findViewById(R.id.divide_line);
            }
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = space;
        }
    }

}