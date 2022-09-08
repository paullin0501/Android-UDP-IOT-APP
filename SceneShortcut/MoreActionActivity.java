package com.rexlite.rexlitebasicnew.SceneShortcut;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.ClickListener;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.SceneSettingActivity;

public class MoreActionActivity extends AppCompatActivity {
    byte[] maxSceneSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    private static final String TAG = "moreAction";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(MoreActionActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_action);
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
                Intent intent = new Intent(MoreActionActivity.this, SceneSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN",maxSceneSN);
                bundle.putString("deviceName",deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideRight(MoreActionActivity.this);
            }
        });

        //裝置名稱
        title.setText("More action");

        //畫面設置
        String[] action = {"Sleeping mode","Shortcut setting"};
        ActionAdapter actionAdapter = new ActionAdapter(action);
        actionAdapter.setOnItemClickListener(new ClickListener <String>() {
            @Override
            public void onClick(View view, String data, int position) {
                if(data.equals("Sleeping mode")){
                    Intent intent = new Intent(MoreActionActivity.this, SceneSleepingModeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",maxSceneSN);
                    bundle.putString("deviceName",deviceName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(MoreActionActivity.this);
                }
                if(data.equals("Shortcut setting")){
                    Intent intent = new Intent(MoreActionActivity.this, SceneShortcutMainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",maxSceneSN);
                    bundle.putString("deviceName",deviceName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(MoreActionActivity.this);
                }
            }
        });
        /*actionAdapter.setOnItemClickListener(new ClickListener<String>(){
            @Override
            public void onClick(View view, String data, int position) {
                Log.d(TAG, "onClick: "+data);
                if(data.equals("Sleeping mode")){
                    Intent intent = new Intent(MoreActionActivity.this, SceneSleepingModeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",maxSceneSN);
                    bundle.putString("deviceName",deviceName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                if(data.equals("Shortcut setting")){
                    Intent intent = new Intent(MoreActionActivity.this, SceneShortcutMainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("maxSceneSN",maxSceneSN);
                    bundle.putString("deviceName",deviceName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });*/
        RecyclerView recyclerView = findViewById(R.id.moreaction_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(actionAdapter);
    }
}