package com.rexlite.rexlitebasicnew.SceneShortcut;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rexlite.rexlitebasicnew.ClickListener;
import com.rexlite.rexlitebasicnew.MainActivity;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;
import com.rexlite.rexlitebasicnew.SceneSettingActivity;
import com.rexlite.rexlitebasicnew.UDP;

import java.util.ArrayList;
import java.util.List;

public class SceneShortcutMainActivity extends AppCompatActivity {
    byte[] maxSceneSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    private List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    int sceneNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_shortcut_main);
        //將裝置的值保留
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        maxSceneSN = info.getByteArray("maxSceneSN");
        deviceName = info.getString("deviceName");
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        //返回按鍵設定
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SceneShortcutMainActivity.this, MoreActionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN",maxSceneSN);
                bundle.putString("deviceName",deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //標題名稱
        title.setText("Shortcut setting");
        new Thread(()->{
            shortcutData =  ShortcutDataBase.getInstance(SceneShortcutMainActivity.this).getShortcutDataDao().fuzzySearchDeviceCH("SCENE", UDP.byteArrayToHexStr(maxSceneSN));

          /*  if(shortcutData.size()==0){
                for(int i = 1 ; i < 7 ; i++){
                    Shortcut shortcut = new Shortcut();
                    shortcut.setName("SCENE "+i);
                    shortcutData.add(shortcut);
                }
                Log.d("shortcutscene", ": "+shortcutData);
            }*/
        }).start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SceneShortcutAdapter sceneShortcutAdapter = new SceneShortcutAdapter(shortcutData);
                sceneShortcutAdapter.setOnItemClickListener(new ClickListener<Shortcut>() {
                    @Override
                    public void onClick(View view, Shortcut data, int position) {
                            Intent intent = new Intent(SceneShortcutMainActivity.this, SceneShortcutSettingActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("maxSceneSN",maxSceneSN);
                            bundle.putString("deviceName",deviceName);
                            bundle.putString("sceneName",data.getName());
                            bundle.putString("sceneNum", data.getDeciveCH());
                            intent.putExtras(bundle);
                            startActivity(intent);
                    }
                });
                RecyclerView recyclerView = findViewById(R.id.shortcutmain_recyclerview);
                recyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(SceneShortcutMainActivity.this, DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(sceneShortcutAdapter);
            }
        },50);


    }
}