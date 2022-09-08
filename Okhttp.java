package com.rexlite.rexlitebasicnew;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class Okhttp {
    private static Okhttp netokhttp;
    public final OkHttpClient client;

    private Okhttp() {
        client = initOkHttpClient();
    }

    /*
    * 1、創建OkHttpClient對象並初始化的封裝。
     在整個項目中我們只需要一個OkHttpClient對象，
     * 不同的網絡請求只需要創建不同的Requset對象和Call對象。
    * */
    private OkHttpClient initOkHttpClient() {

        //初始化的時候可以自定義一些參數
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(2000, TimeUnit.MILLISECONDS)//設置讀取超時為2秒
                .connectTimeout(3000, TimeUnit.MILLISECONDS)//設置鏈接超時為3秒
                .build();
        return okHttpClient;
    }

    /*當第一次使用Okhttp類時，
     Okhttp的對像被創建，並且始終只有一個實例，
     okHttpClient作為Okhttp對象的成員變量，
     也只有一個實例*/
    public static Okhttp postNetokhttp() {
        if (netokhttp == null) {
            netokhttp = new Okhttp();
        }
        return netokhttp;
    }

    /*2.發送網絡請求的封裝
    每次執行網絡訪問的方法一樣
    傳入的參數不一樣
    可以修改自己想傳入的參數類型
*/
//使用方法
//Okhttp.postNetokhttp().callNet(url, json, new Callback(){....});就可以發起網路請求了
    public void callNet(String url, String json, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.d("http", "callNet: "+bodyToString(request));
        Call call = postNetokhttp().initOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
