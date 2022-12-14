package com.rexlite.rexlitebasicnew;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "login";
    WifiManager wifiManager;
    String edRemoteIp = "192.168.1.1";
    String edRemotePort = "4000";
    String receiveMessage;
    Timer timer;
    int time=6;//??????6??????
    final LoadingDialog loadingDialog = new LoadingDialog(LoginActivity.this);
    private EditText edSsid;
    private EditText edPasswd;
    DeviceCommand deviceCommand = new DeviceCommand();
    boolean isAdd = false;
    ExecutorService exec = Executors.newCachedThreadPool();
    MyBroadcast myBroadcast = new MyBroadcast();
    StringBuffer stringBuffer = new StringBuffer();
    TCPServer tcpServer;
    TCPClient tcpClient;
    private CodeScanner mCodeScanner;
    WifiBroadcastReceiver wifiBroadcastReceiver = new WifiBroadcastReceiver();
    ConnectivityManager.NetworkCallback mNetwork;
    AutoCompleteTextView editSSID;
    List<String> ssidList = new ArrayList<String>();
    /**
     * ??????handler????????????????????????
     */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            wifiScan();
        }
    };
    /**
     * ??????Runnable, ???????????????????????????
     */
    Runnable searchWifi = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(1);
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        timer = new Timer();

        /*getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);*/ //???????????????
        //edSsid = findViewById(R.id.ssid);
        edPasswd = findViewById(R.id.passwd);
        editSSID = findViewById(R.id.autocomplete_SSID);
        /**??????????????????*/
        getPermission();
        /**??????WifiManager*/
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        /**????????????*/
        handler.post(searchWifi);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ssidList);
        editSSID.setAdapter(adapter);
        //??????TCP???????????????
        setServerSwitch();
        //??????????????????????????????????????????????????????
        IntentFilter intentFilter = new IntentFilter(TCPServer.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);


    }

    public void login(View view) {
        String ssid = editSSID.getText().toString();
        String passwd = edPasswd.getText().toString();
        //MainActivity.logon = true;
        loadingDialog.startLoadingDialog();
        setSendFunction(editSSID.getText().toString(), edPasswd.getText().toString());
        //setResult(RESULT_OK);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSendFunction(editSSID.getText().toString(), edPasswd.getText().toString());
            }
        }, 500);

       /* for (int i = 0; i < 5; i++) {
            if (receiveMessage == null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSendFunction(editSSID.getText().toString(), edPasswd.getText().toString());
                    }
                }, 1000);

            }
        }*/
        //??????5?????????????????????
        final TimerTask task = new TimerTask(){
            @Override
            public void run() {
                if (receiveMessage == null) {
                    if (time > 0) {
                        time--;
                        setSendFunction(editSSID.getText().toString(), edPasswd.getText().toString());
                    }
                    if (time == 0) {
                        loadingDialog.dismissDialog();
                        new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme)
                                .setTitle("Error")
                                .setMessage("MaxSceneWifi????????????????????????")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        };
        timer.schedule(task,0,1000);

       // loadingDialog.dismissDialog();

        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         *//**Android10(API29???)???????????????????????????????????????*//*
             connectWifiQ(ssid, passwd);

          *//*  MainActivity.logon = true;
            Intent intent = new Intent(LoginActivity.this,SlashActivity.class);
            startActivity(intent);*//*
           // connect(ssid,passwd);

        } else {
            *//**Android10(API28???)???????????????????????????????????????*//*
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            getApplicationContext().registerReceiver(wifiBroadcastReceiver, filter);
            connectWifi(ssid, passwd);
        }*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        /**???????????????????????????*/
        handler.removeCallbacks(searchWifi);
        LocalBroadcastManager.getInstance(LoginActivity.this).unregisterReceiver(myBroadcast);
        /**???????????????????????????Wifi*/
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /**??????Android10?????????????????????????????????*/
               /* @SuppressLint("ServiceCast")
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                assert connectivityManager != null;
                connectivityManager.unregisterNetworkCallback(mNetwork);*/
            } else {
                /**???Android10??????????????????WifiManager?????????*/
               /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration configuration : list){
                    wifiManager.removeNetwork(configuration.networkId);
                }
                wifiManager.disconnect();
                unregisterReceiver(wifiBroadcastReceiver);*/
            }
        } catch (Exception e) {
            Log.i(TAG, "onStop: " + e.toString());
        }


    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100
            );
        }//if
    }

    private void wifiScan() {
        new Thread(() -> {
            /**??????Wifi??????????????????*/
            wifiManager.setWifiEnabled(true);
            /**????????????*/
            wifiManager.startScan();
            /**??????????????????Wifi*/
            List<ScanResult> wifiList = wifiManager.getScanResults();
            if (!isAdd) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult s = wifiList.get(i);
                    Log.d(TAG, "run: " + s.SSID + "\n");
                    if (s.SSID.length() > 0) {
                        ssidList.add(s.SSID);
                        /*for (int j=0 ; j<ssidList.size() ; j++){

                            if(!ssidList.get(j).equals(s.SSID)){

                            }
                        }*/

                    }
               /* Log.d(TAG, "run: "+s.SSID+"\n"
                        +s.BSSID+"\n"
                        +s.capabilities+"\n"
                        +s.centerFreq0+"\n"
                        +s.centerFreq1+"\n"
                        +s.channelWidth+"\n"
                        +s.frequency+"\n"
                        +s.level+"\n"
                        +s.operatorFriendlyName+"\n"
                        +s.timestamp+"\n"
                        +s.venueName+"\n");*/

                }
                isAdd = true;
            }
        }).start();


    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWifiQ(String ssid, String password) {
        // startActivity(new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"));
        /**Android10???????????????????????????WifiNetworkSpecifier*/
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();
        NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                        .setNetworkSpecifier(specifier)
                        .build();

        @SuppressLint("ServiceCast")
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        mNetwork = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);


                assert connectivityManager != null;
                /**??????????????????????????????Wifi*/
                connectivityManager.bindProcessToNetwork(network);
                // setSendFunction(editSSID.getText().toString(),edPasswd.getText().toString());
                getSharedPreferences("Data",Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("log",true)
                        .commit();
                MainActivity.logon = true;
                Intent intent = new Intent(LoginActivity.this,SlashActivity.class);
                startActivity(intent);

            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.w(TAG, "onUnavailable: ????????????");
            }

        };

        connectivityManager.requestNetwork(request, mNetwork);
       /* NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                        Intent i = new Intent(LoginActivity.this,SlashActivity.class);
                        startActivity(i);
                    // connected to wifi
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    break;
                default:
                    break;
            }
        } else {
            // not connected to the internet
        }*/

    }

    /**
     * Android10????????????
     */
    private void connectWifi(String tagSsid, String tagPassword) {
        String ssid = "\"" + tagSsid + "\"";
        String password = "\"" + tagPassword + "\"";
        WifiConfiguration conf = new WifiConfiguration();
        conf.allowedProtocols.clear();
        conf.allowedAuthAlgorithms.clear();
        conf.allowedGroupCiphers.clear();
        conf.allowedKeyManagement.clear();
        conf.allowedPairwiseCiphers.clear();
        conf.SSID = ssid;
        conf.preSharedKey = password;
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiManager.addNetwork(conf);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration : list) {
            if (configuration.SSID != null && conf.SSID.equals(ssid)) {
                /**???????????????Wifi*/
                wifiManager.disconnect();
                /**???????????????Wifi*/
                wifiManager.enableNetwork(conf.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }

    }

    /**
     * ??????TCP???????????????
     */
    private void setServerSwitch() {
        tcpClient = new TCPClient(edRemoteIp, Integer.parseInt(edRemotePort), this);
        exec.execute(tcpClient);
    }

    /**
     * ????????????????????????(?????????)
     */

    private void setSendFunction(String ssid, String password) {
        byte[] cmd = deviceCommand.MaxScene_APMode(ssid, password);
        String text = ssid + password;
        if (tcpClient == null) return;
        // if (text.length() == 0 || !tcpClient.getStatus()) return;
        if (cmd.length == 0 || !tcpClient.getStatus()) return;
        exec.execute(() -> tcpClient.send(cmd));
        Log.d(TAG, "????????????: " + UDP.byteArrayToHexStr(cmd));
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.logon = true;
                Intent intent = new Intent(LoginActivity.this,SlashActivity.class);
                startActivity(intent);
            }
        }, 500);*/
        // Log.d(TAG, "setSendFunction: "+UDP.byteArrayToHexStr(text));
//           exec.execute(new Runnable() {
//               @Override
//               public void run() {
//                   tcpServer.SST.get(0).sendData(text);
//               }
//           });


    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            assert mAction != null;
            /**????????????TCP???????????????*/
            switch (mAction) {
                case TCPServer.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(TCPServer.RECEIVE_STRING);
                    byte[] bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES);
                    stringBuffer.append("????????? ").append(msg).append("\n");
                    receiveMessage = stringBuffer.toString();
                    //  Log.d(TAG, "????????????: "+UDP.byteArrayToHexStr(bytes));
                   /* if(bytes.length>0){

                    }*/
                    if (receiveMessage.length() > 0) {
                        tcpClient.closeClient();
                        loadingDialog.dismissDialog();
                        Log.d(TAG, "?????????: ");
                        // tcpServer.closeServer();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            /**Android10(API29???)???????????????????????????????????????*/
                            connectWifiQ(editSSID.getText().toString(), edPasswd.getText().toString());

                        } else {
                            /**Android10(API28???)???????????????????????????????????????*/
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                            getApplicationContext().registerReceiver(wifiBroadcastReceiver, filter);
                            connectWifi(editSSID.getText().toString(), edPasswd.getText().toString());
                        }


                    }
                    Log.d(TAG, "onReceive: " + receiveMessage);

                    break;

            }
        }
    }

    /**
     * ??????Wifi???????????????
     */
    private class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.i(TAG, "Wifi?????????");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.i(TAG, "??????Wifi???");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.i(TAG, "Wifi?????????");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.i(TAG, "??????Wifi???");
                        break;
                }
            } else if ((WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()))) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                assert info != null;
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {
                    Toast.makeText(context, "Wifi?????????", Toast.LENGTH_SHORT).show();
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {
                    setSendFunction(editSSID.getText().toString(), edPasswd.getText().toString());
                    Toast.makeText(context, "Wifi?????????", Toast.LENGTH_SHORT).show();
                    MainActivity.logon = true;
                    Intent intent1 = new Intent(LoginActivity.this,SlashActivity.class);
                    startActivity(intent1);
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {
                    Log.i(TAG, "Wifi?????????");
                }
            }
        }
    }

}
