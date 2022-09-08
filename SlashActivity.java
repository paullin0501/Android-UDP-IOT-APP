package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class SlashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slash);
        /*getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);*/ //將標題隱藏
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.logon = true;
                startActivity(new Intent(SlashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }

}
