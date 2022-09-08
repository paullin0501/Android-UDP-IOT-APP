package com.rexlite.rexlitebasicnew.SceneShortcut;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;
import com.rexlite.rexlitebasicnew.RoomDataBase.ShortcutDataBase;
import com.rexlite.rexlitebasicnew.UDP;

import java.util.ArrayList;
import java.util.List;

public class SceneShortcutSettingActivity extends AppCompatActivity {
    byte[] maxSceneSN;//點擊該裝置時傳送對應的SN
    String deviceName;
    String sceneName;
   String sceneNum;
   EditText edName;
   boolean checked = false;
   boolean isShortcut = false;
    byte maxType = 0x0b;
    private RadioGroup mGroup1;
    private RadioGroup mGroup2;
    List<Shortcut> shortcutData = new ArrayList<Shortcut>();
    Shortcut shortcut = new Shortcut();
    private static final String TAG = "sceneicon";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_shortcut_setting);
        //將裝置的值保留
        Intent intent = getIntent();
        Bundle info = intent.getExtras();
        maxSceneSN = info.getByteArray("maxSceneSN");
        deviceName = info.getString("deviceName");
        sceneName = info.getString("sceneName");
        sceneNum = info.getString("sceneNum");
        mGroup1 = findViewById(R.id.radio_group_1);
        mGroup2 = findViewById(R.id.radio_group_2);
        edName = findViewById(R.id.ed_name);
        edName.setText(sceneName);
        edName.requestFocus();
        //選單設定
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title = findViewById(R.id.toolbar_title);
        //加入捷徑按鈕邏輯
        Switch sw_btn = findViewById(R.id.switch1);
        //先檢查是否有加入過捷徑
        new Thread(() -> {
            shortcutData = ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN), sceneNum,String.format("%02X ", maxType),true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(shortcutData.size()>0){
                        handleStartUi(shortcutData.get(0).getIcon());
                        isShortcut  = true;
                        sw_btn.setChecked(true);
                        checked = true;
                    }
                    if(shortcutData.size()==0){
                        handleStartUi(R.drawable.scene_icon1_on);
                    }
                }
            });
        }).start();

        sw_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checked = isChecked;
                } else {
                    checked =isChecked;
                }
            }
        });
        //裝置名稱
        title.setText(sceneName);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //代表要加入捷徑
                if(checked==true){
                    Log.d(TAG, "getText: "+edName.getText().toString());
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData =  ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().findByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN),sceneNum,String.format("%02X ", maxType));
                        if(shortcutCheckData.size()>0) {
                            ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().updateMaxSceneShortcut(shortcutCheckData.get(0).getId(), shortcut.getIcon(), true,edName.getText().toString());

                        }
                    }).start();
                    //代表不加入或移除(只更改名稱)
                } else{
                    new Thread(() -> {
                        List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                        shortcutCheckData =  ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().findDataByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN),sceneNum,String.format("%02X ", maxType),true);
                        if(shortcutCheckData.size()>0) {
                            int id = shortcutCheckData.get(0).getId();
                            ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().updateMaxSceneNameAndShow(id,edName.getText().toString(),false);
                        }
                    }).start();
                }

                Intent intent = new Intent(SceneShortcutSettingActivity.this, SceneShortcutMainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("maxSceneSN", maxSceneSN);
                bundle.putString("deviceName", deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateSlideRight(SceneShortcutSettingActivity.this);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //代表要加入捷徑
        if(checked==true){
            Log.d(TAG, "getText: "+edName.getText().toString());
            new Thread(() -> {
                List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                shortcutCheckData =  ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().findByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN),sceneNum,String.format("%02X ", maxType));
                if(shortcutCheckData.size()>0) {
                    ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().updateMaxSceneShortcut(shortcutCheckData.get(0).getId(), shortcut.getIcon(), true,edName.getText().toString());

                }
            }).start();
            //代表不加入或移除(只更改名稱)
        } else{
            new Thread(() -> {
                List<Shortcut> shortcutCheckData = new ArrayList<Shortcut>();
                shortcutCheckData =  ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().findByCHandSNandType(UDP.byteArrayToHexStr(maxSceneSN),sceneNum,String.format("%02X ", maxType));
                if(shortcutCheckData.size()>0) {
                    int id = shortcutCheckData.get(0).getId();
                    ShortcutDataBase.getInstance(SceneShortcutSettingActivity.this).getShortcutDataDao().updateMaxSceneNameAndShow(id,edName.getText().toString(),false);
                }
            }).start();
        }

        Intent intent = new Intent(SceneShortcutSettingActivity.this, SceneShortcutMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putByteArray("maxSceneSN", maxSceneSN);
        bundle.putString("deviceName", deviceName);
        intent.putExtras(bundle);
        startActivity(intent);
        Animatoo.animateSlideRight(SceneShortcutSettingActivity.this);
    }

    public void handleCombinedClick(View view) {
        // Clear any checks from both groups:
        mGroup1.clearCheck();
        mGroup2.clearCheck();

        // Manually set the check in the newly clicked radio button:
        ((RadioButton) view).setChecked(true);

        // Perform any action desired for the new selection:
        switch (view.getId()) {
            case R.id.icon1_btn:
                shortcut.setIcon(R.drawable.scene_icon1_on);
                break;

            case R.id.icon2_btn:
                shortcut.setIcon(R.drawable.scene_icon2_on);
                break;
            case R.id.icon3_btn:
                shortcut.setIcon(R.drawable.scene_icon3_on);
                break;

            case R.id.icon4_btn:
                shortcut.setIcon(R.drawable.scene_icon4_on);
                break;

            case R.id.icon5_btn:
                shortcut.setIcon(R.drawable.scene_icon5_on);
                break;
            case R.id.icon6_btn:
                shortcut.setIcon(R.drawable.scene_icon6_on);
                break;

            case R.id.icon7_btn:
                shortcut.setIcon(R.drawable.scene_icon7_on);
                break;
            case R.id.icon8_btn:
                shortcut.setIcon(R.drawable.scene_icon8_on);
                break;
            case R.id.icon9_btn:
                shortcut.setIcon(R.drawable.scene_icon9_on);
                break;

        }
    }
    public void handleStartUi(int icon) {
        // Clear any checks from both groups:
        mGroup1.clearCheck();
        mGroup2.clearCheck();
      /*  // Manually set the check in the newly clicked radio button:
        ((RadioButton) view).setChecked(true);*/

        // Perform any action desired for the new selection:
        switch (icon) {
            case R.drawable.scene_icon1_on:
                shortcut.setIcon(R.drawable.scene_icon1_on);
                mGroup1.check(R.id.icon1_btn);
                break;

            case R.drawable.scene_icon2_on:
                shortcut.setIcon(R.drawable.scene_icon2_on);
                mGroup1.check(R.id.icon2_btn);
                break;
            case R.drawable.scene_icon3_on:
                shortcut.setIcon(R.drawable.scene_icon3_on);
                mGroup1.check(R.id.icon3_btn);
                break;

            case R.drawable.scene_icon4_on:
                shortcut.setIcon(R.drawable.scene_icon4_on);
                mGroup1.check(R.id.icon4_btn);
                break;

            case R.drawable.scene_icon5_on:
                shortcut.setIcon(R.drawable.scene_icon5_on);
                mGroup1.check(R.id.icon5_btn);
                break;
            case R.drawable.scene_icon6_on:
                shortcut.setIcon(R.drawable.scene_icon6_on);
                mGroup2.check(R.id.icon6_btn);
                break;

            case R.drawable.scene_icon7_on:
                shortcut.setIcon(R.drawable.scene_icon7_on);
                mGroup2.check(R.id.icon7_btn);
                break;
            case R.drawable.scene_icon8_on:
                shortcut.setIcon(R.drawable.scene_icon8_on);
                mGroup2.check(R.id.icon8_btn);
                break;
            case R.drawable.scene_icon9_on:
                shortcut.setIcon(R.drawable.scene_icon9_on);
                mGroup2.check(R.id.icon9_btn);
                break;

        }
    }

}
