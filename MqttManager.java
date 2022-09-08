
package com.rexlite.rexlitebasicnew;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mqtt工具
 */
public class MqttManager {

    private Context mContext;
    private Handler mHandler;
    private static MqttManager mMqttManager;
    private  MqttAndroidClient mMqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    //MQTT相關配置
    //服務器地址（協議+IP+Port號）
    private final static String HOST = "tcp://35.224.34.117:1883";
    //用户名
    public   static String USERNAME = "test";
    //密碼
    private final static String PASSWORD = "12345678";
    //發布主題
    public static String PUBLISH_TOPIC = "/r";
    public static String version_TOPIC="/r";
    public static String status_topic="/r";
    public static String progress_topic="/r";
    public static String upgrade_topic="/r";
    public static String update_topic="/r";


    //訂閱主題
    public static String RESPONSE_TOPIC = "/b";
    //服務質量,0最多一次，1最少一次，2只一次
    private final static int QUALITY_OF_SERVICE = 0;
    //重連hand
    public final static int HANDLE_CONNECT_FAILED = 0x1000;
    public final static int HANDLE_CONNECT_BROKEN = 0x1001;

    //取得的訊息
    public static String jsonString;
    public static String progressString;
    public static String statusString;
    public static String upgradeString;

    public boolean isConnect(){
       return   mMqttAndroidClient.isConnected();
    }
    /**
     * 構造函數
     *
     * @param context
     */
    public MqttManager(Context context) {
        this.mContext = context;
        init();
    }

    /**
     * 單例模式
     */
    public static MqttManager getInstance(Context context) {
        if (mMqttManager == null) {
            mMqttManager = new MqttManager(context);
        }
        return mMqttManager;
    }

    /**
     * 初始化
     */
    private void init() {
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //設置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(10); //設置超過時間，單位：秒
        mMqttConnectOptions.setKeepAliveInterval(30); //設置心跳包發送間隔，單位：秒
        mMqttConnectOptions.setUserName(USERNAME); //設置用户名
       // mMqttConnectOptions.setAutomaticReconnect(true);
        mMqttConnectOptions.setPassword(PASSWORD.toCharArray()); //設置密码
        // 異常斷開發送的信息
        String message = "{\"Dced\":\"" + 1 + "\"}";
        String topic = PUBLISH_TOPIC;
        Integer qos = QUALITY_OF_SERVICE;
        Boolean retained = false;
        try {
            //mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MQTT是否連接成功的監聽
     */
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i("--->mqtt", "連接成功 ");
            try {
                //訂閱主題，参數：主题、服務質量
                String[] topicArray = {version_TOPIC, status_topic, progress_topic, upgrade_topic};
                int[] qosArray = {0, 0, 0, 0};
                mMqttAndroidClient.subscribe(topicArray, qosArray);
                Log.i("--->mqtt", "訂閱成功 ");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("--->mqtt", "訂閱失敗 ");
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i("--->mqtt", "onFailure 連接失敗:" + arg1.getMessage());
            sendHandlerMsg(HANDLE_CONNECT_FAILED, 0, null);
        }
    };

    public void getJSONInfo(String message, int type) {
        if (type == 1) {
            jsonString = message;
        }
        if (type == 2) {
            progressString = message;
        }
        if (type == 3) {
            statusString = message;
        }
        if (type == 4) {
            upgradeString = message;
        }
    }

    /**
     * 訂閱主题的回調
     */
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Integer qos = QUALITY_OF_SERVICE;
            Boolean retained = false;
          /*  Log.i("--->mqtt", "收到消息： " + new String(message.getPayload()) + "\tToString:" + message.toString()+"topic: "+topic);
            Log.i("--->mqtt", "topic1: "+topic.equals(status_topic)+"topic2: "+version_TOPIC+ "topic3:"+upgrade_topic +"topic4:"+progress_topic );*/
            //根據主題將消息分類
            if (topic.equals(version_TOPIC)) {
                Log.i("--->mqtt", "收到消息： " + new String(message.getPayload()) + "\tToString:" + message.toString());
                getJSONInfo(new String(message.getPayload()), 1);
            }
            if (topic.equals(progress_topic)) {
                Log.i("--->mqtt", "收到progress： " + new String(message.getPayload()) + "\tToString:" + message.toString());
                getJSONInfo(new String(message.getPayload()), 2);
                //發送eventbus消息
                EventBus.getDefault().post(message.toString());
            }
            if (topic.equals(status_topic)) {
                Log.i("--->mqtt", "收到status： " + new String(message.getPayload()) + "\tToString:" + message.toString());
                //當status跳到READY狀態自動發出更新指令
                if (new String(message.getPayload()).equals("READY")) {
                    mMqttAndroidClient.publish(update_topic, "1".getBytes(), qos.intValue(), retained.booleanValue());
                    EventBus.getDefault().post("PREPARE");
                }
                if(new String(message.getPayload()).equals("FINISH")){
                    mMqttAndroidClient.subscribe(version_TOPIC, qos.intValue());
                    getJSONInfo(new String(message.getPayload()), 1);
                }
                else {
                    //發送eventbus消息
                    EventBus.getDefault().post(message.toString());
                }
                getJSONInfo(new String(message.getPayload()), 3);


            }
            if (topic.equals(upgrade_topic)) {
                Log.i("--->mqtt", "收到upgrade： " + new String(message.getPayload()) + "\tToString:" + message.toString());
                getJSONInfo(new String(message.getPayload()), 4);
                UpdateStatus updateStatus = new UpdateStatus();
                updateStatus.setUpgrade(message.toString());
                EventBus.getDefault().post(updateStatus);
            }

            //收到其他客户端的訊息後，響應给對方告知訊息已到達或者消息有問题等
            //response("message arrived:"+message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            try {
                Log.i("--->mqtt", "deliveryComplete" + arg0.getMessage().toString()
                        + "|"
                        + arg0.isComplete());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i("--->mqtt", "連接斷開");
            sendHandlerMsg(HANDLE_CONNECT_BROKEN, 0, null);
        }
    };

    /*    */

    /**
     * 訂閱消息
     *
     * @param topic 訂閱消息的主题
     */
    public void subscribeMsg(String topic, int qos) {
        if (mMqttAndroidClient != null) {
            int[] Qos = {qos};
            String[] topic1 = {topic};
            try {
                mMqttAndroidClient.subscribe(topic1, Qos);
                Log.i("--->mqtt", "訂閱主題" + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 建立mqtt連接，連接MQTT服務器
     */
    public boolean connect(String clientId, Handler handler) {
        this.mHandler = handler;
        //創建Mqtt客户端
        mMqttAndroidClient = new MqttAndroidClient(mContext, HOST, clientId);
        Log.i("--->mqtt", "new client");
        mMqttAndroidClient.setCallback(mqttCallback); //設置訂閱消息的回調
        Log.i("--->mqtt", "set callback");
        //建立連接
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
                    Log.i("--->mqtt", "mqtt connecting");
                } catch (Exception e) {
                    Log.e("--->mqtt", "connect: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();




        /*try {
            mMqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
        } catch (Exception e) {
            Log.e("--->mqtt","connect: " + e.getMessage());
            e.printStackTrace();
            return false;
        }*/
        return true;
    }

    /**
     * 發布消息
     */
    public void publish(String message, String topic) {
        Integer qos = QUALITY_OF_SERVICE;
        Boolean retained = false;
        try {
            if (mMqttAndroidClient != null && mMqttAndroidClient.isConnected()) {
                //參數分別為：主题、消息的字節數組、服務質量、是否在服務器保留斷開連接後的最後一則訊息
                mMqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
                Log.i("--->mqtt", topic);
            } else {
                Log.i("--->mqtt", "mqttAndroidClient is Null or is not connected");
            }
        } catch (Exception e) {
            Log.i("--->mqtt", "publish MqttException:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 響應訂閱訊息
     */
    public void response(String message) {
        String topic = RESPONSE_TOPIC;
        Integer qos = QUALITY_OF_SERVICE;
        Boolean retained = false;
        try {
            //參數分別為：主题、消息的字節數組、服務質量、是否在服務器保留段開連接後的最後一則訊息
            mMqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (Exception e) {
            Log.i("--->mqtt", "publish:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 斷開連接
     */
    public void disconnect() {
        try {
            if (mMqttAndroidClient != null) {
                mMqttAndroidClient.unregisterResources();
                mMqttAndroidClient.disconnect();
                mMqttAndroidClient = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 發送handler訊息
     */
    private void sendHandlerMsg(int arg1, int arg2, Object obj) {
        if (mHandler == null) {
            return;
        }
        Message message = new Message();
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.obj = obj;
        mHandler.sendMessage(message);
    }

}