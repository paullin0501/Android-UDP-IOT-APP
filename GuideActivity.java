package com.rexlite.rexlitebasicnew;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.WifiParsedResult;
import com.journeyapps.barcodescanner.camera.CameraManager;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.barcode.Intents;
import com.rexlite.rexlitebasicnew.barcode.ResultHandler;
import com.rexlite.rexlitebasicnew.barcode.ResultHandlerFactory;
import com.rexlite.rexlitebasicnew.barcode.WifiConfigManager;
import com.rexlite.rexlitebasicnew.barcode.WifiResultHandler;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.google.zxing.client.result.ResultParser.parseResult;

public class GuideActivity extends AppCompatActivity  {
    private static final String TAG = "guide";
    private EditText edDeviceid;
    static WifiManager wifiManager;
    private EditText edPasswd;
    public DataBase dataBase;
    static boolean isAdd = false;
    private CodeScanner mCodeScanner;
    private CameraManager cameraManager;
    private GuideActivityHandler guidehandler;
    ZXingScannerView zXingScannerView;
    DeviceCommand deviceCommand = new DeviceCommand();
    GuideActivity.WifiBroadcastReceiver wifiBroadcastReceiver = new GuideActivity.WifiBroadcastReceiver();
    ConnectivityManager.NetworkCallback mNetwork;
    AutoCompleteTextView editSSID;
    static List<String> ssidList = new ArrayList<String>();
    public ArrayAdapter<String> adapter;
    /**賦予handler重複執行掃描工作*/
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            wifiScan();
        }
    };
    /**建立Runnable, 使掃描可被重複執行*/
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
        setContentView(R.layout.activity_guide);
        edPasswd = findViewById(R.id.passwd);
        editSSID = findViewById(R.id.autocomplete_SSID);

//        TextView animTest = findViewById(R.id.test);
        if(MainActivity.udpServer!=null) {
            MainActivity.udpServer.changeServerStatus(false);
        }
       /* byte[] cmd = deviceCommand.MaxScene_APMode("Sky tree 23B","23111195");
        Log.d(TAG, "password: "+UDP.byteArrayToHexStr(cmd));*/
        /**取得所需權限*/
        //getPermission();
        requestPermission_multiple();
        /**取得WifiManager*/
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ssidList);
        editSSID.setAdapter(adapter);
       // zXingScannerView = findViewById(R.id.ZXingScannerView_QRCode);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        //qrcode掃描

        mCodeScanner = new CodeScanner(this, scannerView);

    /*    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    100);
        }*/ //else{
            //若先前已取得權限，則直接開啟
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(GuideActivity.this, result);
                        WifiParsedResult wifiResult = (WifiParsedResult) resultHandler.getResult();
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                /**Android10(API29↑)以上版本的手機要執行的部分*/
                                //  startActivity(new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"));
                                connectWifiQ(wifiResult.getSsid(),wifiResult.getPassword());



                            } else {
                                /**Android10(API28↓)以下版本的手機要執行的部分*/
                                IntentFilter filter = new IntentFilter();
                                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                                getApplicationContext().registerReceiver(wifiBroadcastReceiver, filter);
                                connectWifi(wifiResult.getSsid(), wifiResult.getPassword());
                            }
                            Toast.makeText(GuideActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        //}

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
        //取得相機權限
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    100);
        }else{
            //若先前已取得權限，則直接開啟
            openQRCamera();

        }*/
     /*   animTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(GuideActivity.this,TestAnimActivity.class));
                new Thread(() -> {
                    DataBase.getInstance(GuideActivity.this).getDataUao().nukeTable();
                }).start();
                new Thread(() -> {
                DataBase.getInstance(GuideActivity.this).clearAllTables();
                }).start();
            }
        });*/
    }
    public void guide(View view) {
        String ssid = editSSID.getText().toString();
        String passwd = edPasswd.getText().toString();

        //setResult(RESULT_OK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            /**Android10(API29↑)以上版本的手機要執行的部分*/
          //  startActivity(new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"));
              connectWifiQ(ssid, passwd);
          /*  MainActivity.logon = true;
            Intent intent = new Intent(LoginActivity.this,SlashActivity.class);
            startActivity(intent);*/


        } else {
            /**Android10(API28↓)以下版本的手機要執行的部分*/
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            getApplicationContext().registerReceiver(wifiBroadcastReceiver, filter);
            connectWifi(ssid, passwd);
        }

    }

   /* *//**取得權限回傳*//*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults[0] ==0){
            openQRCamera();
        }else{
        }
    }
    *//**開啟QRCode相機*//*
    private void openQRCamera(){
       *//* zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();*//*
    }
    *//**取得QRCode掃描到的物件回傳*//*
    @Override
    public void handleResult(Result rawResult) {
        //ZXing相機預設掃描到物件後就會停止，以此這邊再次呼叫開啟，使相機可以為連續掃描之狀態
        Log.d(TAG, "handleResult: "+rawResult);
        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

        if(resultHandler.getType()== ParsedResultType.WIFI){
            Log.d(TAG, "handleResult: "+resultHandler);
            WifiParsedResult wifiResult = (WifiParsedResult) resultHandler.getResult();
            Log.d(TAG, "handleResult: "+wifiResult.getPassword());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                *//**Android10(API29↑)以上版本的手機要執行的部分*//*
                //  startActivity(new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"));
                connectWifiQ(wifiResult.getSsid(),wifiResult.getPassword());



            } else {
                *//**Android10(API28↓)以下版本的手機要執行的部分*//*
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                getApplicationContext().registerReceiver(wifiBroadcastReceiver, filter);
                connectWifi(wifiResult.getSsid(), wifiResult.getPassword());
            }
          //  new WifiConfigManager(wifiManager).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,wifiResult);
           *//* Intent intent = new Intent(getIntent().getAction());
            intent.addFlags(Intents.FLAG_NEW_DOC);
            intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
            intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
            byte[] rawBytes = rawResult.getRawBytes();
            if (rawBytes != null && rawBytes.length > 0) {
                intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
            }
            Map<ResultMetadataType, ?> metadata = rawResult.getResultMetadata();
            if (metadata != null) {
                if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
                    intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION,
                            metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
                }
                Number orientation = (Number) metadata.get(ResultMetadataType.ORIENTATION);
                if (orientation != null) {
                    intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
                }
                String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
                if (ecLevel != null) {
                    intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
                }
                @SuppressWarnings("unchecked")
                Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
                if (byteSegments != null) {
                    int i = 0;
                    for (byte[] byteSegment : byteSegments) {
                        intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
                        i++;
                    }
                }
            }*//*
         //   sendReplyMessage(R.id.return_scan_result, intent, resultDurationMS);
        }

        openQRCamera();
    }
*/
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(searchWifi);
//        zXingScannerView.stopCamera();
        /**跳出畫面則停止掃描*/
        //  handler.removeCallbacks(searchWifi);
        /**斷開現在正連線著的Wifi*/
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /**若為Android10的手機，則在此執行斷線*/
               /* @SuppressLint("ServiceCast")
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                assert connectivityManager != null;
                connectivityManager.unregisterNetworkCallback(mNetwork);*/
            } else {
                /**非Android10手機，則執行WifiManager的斷線*/
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
        }catch (Exception e){
            Log.i(TAG, "onStop: "+e.toString());
        }


    }
   /* public  void guide(View view){
        String ssid = edDeviceid.getText().toString();
        String passwd = edPasswd.getText().toString();
        // setResult(RESULT_OK);
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }*/
    public  void  skip(View view){
        getSharedPreferences("Data",Context.MODE_PRIVATE)
                .edit()
                .putBoolean("log",true)
                .commit();
        Intent intent = new Intent(this,SlashActivity.class);
        startActivity(intent);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.e("1111111", "onRequestPermissionsResult: 單個權限成功" );
                }else{
                    Log.e("1111111", "onRequestPermissionsResult: 單個權限失敗" );
                }
                break;
            case 2:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Log.e("2222222222", "onRequestPermissionsResult: 用戶拒絕了某個權限");
                            return;
                        }
                    }

                    Log.e("2222222222", "onRequestPermissionsResult: 用戶授權所有權限");
                    /**取得權限後執行掃描wifi*/
                    handler.post(searchWifi);
                }else {
                    Log.e("2222222222", "onRequestPermissionsResult:" );
                }
                break;
            default:
                break;
        }
    }

//取得所需權限
    private void requestPermission_multiple(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(GuideActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(GuideActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(GuideActivity.this,permissions,2);
        }else {
            Log.e("222222222", "requestPermission_multiple: 已獲得所有權限!!");
            handler.post(searchWifi);
        }
    }

    private  void wifiScan() {
        Log.d(TAG, "wifiScan: scanning");
        new Thread(() -> {
            /**設置Wifi回傳可被使用*/
            wifiManager.setWifiEnabled(true);
            /**開始掃描*/
            wifiManager.startScan();
            /**取得掃描到的Wifi*/
            List<ScanResult> wifiList = wifiManager.getScanResults();
            if (!isAdd) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult s = wifiList.get(i);
                    Log.d(TAG, "size: "+i);
                    List<String> newSSIDList = new ArrayList<String>();
                    if(s.SSID.length()>0) {
                        //list 利用Set去除重複
                        ssidList.add(s.SSID);
                        HashSet<String> set = new HashSet<>(ssidList);
                        newSSIDList.addAll(set);
                        ssidList = newSSIDList;


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
            for(int k = 0 ;k<ssidList.size();k++) {
                Log.d(TAG, "SSID: " + ssidList.get(k));
            }
            runOnUiThread(()->{
                /**更新掃描後的列表*/
                adapter.addAll(ssidList);
            });
        }).start();
      /*  for (i in 0 until list.size) {
            val position: Int = getItemPosition(result, list[i])
            if (position != -1) {
                if (list[position].level < list[i].level) {
                    result.removeAt(position)
                    result.add(position, list[i])
                }
            } else {
                result.add(list[i])
            }
        }*/


    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWifiQ(String ssid, String password) {
        // startActivity(new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"));
        /**Android10以上的手機必須調用WifiNetworkSpecifier*/
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
                /**將手機網路綁定到指定Wifi*/
                connectivityManager.bindProcessToNetwork(network);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.logon = true;
                        Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
                        startActivity(intent);

                    }
                }, 500);

            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.w(TAG, "onUnavailable: 連線失敗");
            }

        };

        connectivityManager.requestNetwork(request, mNetwork);
    }
    /**Android10↓的連線*/
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
        for (WifiConfiguration configuration : list){
            if (configuration.SSID != null && conf.SSID.equals(ssid)){
                /**斷開原先的Wifi*/
                wifiManager.disconnect();
                /**連接指定的Wifi*/
                wifiManager.enableNetwork(conf.networkId,true);
                wifiManager.reconnect();
                break;
            }
        }

    }

    /**廣播Wifi的所有狀態*/
    private class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)){
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.i(TAG, "Wifi關閉中");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.i(TAG, "關閉Wifi中");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.i(TAG, "Wifi使用中");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.i(TAG, "開啟Wifi中");
                        break;
                }
            }else if((WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()))){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                assert info != null;
                if (NetworkInfo.State.DISCONNECTED == info.getState()){
                    Toast.makeText(context, "Wifi已斷線", Toast.LENGTH_SHORT).show();
                }else if(NetworkInfo.State.CONNECTED == info.getState()){
                    Toast.makeText(context, "Wifi已連接", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(GuideActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }else if(NetworkInfo.State.CONNECTING == info.getState()){
                    Log.i(TAG, "Wifi連線中");
                }
            }
        }
    }
}