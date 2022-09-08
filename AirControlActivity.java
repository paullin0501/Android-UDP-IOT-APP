package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gcssloop.widget.ArcSeekBar;
import com.tenclouds.gaugeseekbar.GaugeSeekBar;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class AirControlActivity extends AppCompatActivity {
    GaugeSeekBar seekBar;
    TextView airText;
    ImageView airPlus,airMinus;
    float settingTemp;
    private static final String TAG = "air";
    TimePicker timePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_control);
        airText = findViewById(R.id.air_text);
        airPlus = findViewById(R.id.air_plus);
        airMinus = findViewById(R.id.air_minus);

        //TimePicker設定
        timePicker = findViewById(R.id.timepicker);
        //拿掉PM AM
        timePicker.setIs24HourView(true);
        Resources systemResources = Resources.getSystem();
        //限制時間的範圍
        int hourNumberPickerId = systemResources.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = systemResources.getIdentifier("minute", "id", "android");
        NumberPicker hourNumberPicker = (NumberPicker) timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = (NumberPicker) timePicker.findViewById(minuteNumberPickerId);
        hourNumberPicker.setMaxValue(12);

        View view = getLayoutInflater().inflate(R.layout.activity_air_control, null);
        seekBar = findViewById(R.id.arc_seek_bar);
        settingTemp = seekBar.getProgress();
        float d =  BigDecimal.valueOf(0.7829f).setScale(3, BigDecimal.ROUND_HALF_DOWN) .floatValue();
        seekBar.setProgress(d);
        Function1<? super Float, Unit> unitFunction1 = new Some(airText);
        seekBar.setProgressChangedCallback(unitFunction1);
        //seekbar的值從1~0
        airPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float f =seekBar.getProgress();
                //四捨五入到小數點後四位
                f = BigDecimal.valueOf(f).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                if(f>1){
                    airText.setText(30+"°C");
                    seekBar.setProgress(1);
                } else {
                    float one = BigDecimal.valueOf(0.0714f).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                    seekBar.setProgress(f+one);
                    float value  = BigDecimal.valueOf(seekBar.getProgress()).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                    airText.setText(String.valueOf(((int)(value*14+16)))+"°C");

                }
                float value  = BigDecimal.valueOf(seekBar.getProgress()).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                Log.d(TAG, "tmp: "+value);
            }
        });
        airMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float f = seekBar.getProgress();
                f = BigDecimal.valueOf(f).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                if(f<0){
                    airText.setText(16+"°C");
                    seekBar.setProgress(0);
                } else {
                    float one =  BigDecimal.valueOf(0.0714f).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                    seekBar.setProgress(f-one);
                    float value  = BigDecimal.valueOf(seekBar.getProgress()).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                    airText.setText(String.valueOf(((int)(value*14+16)))+"°C");

                }
                float value  = BigDecimal.valueOf(seekBar.getProgress()).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
                Log.d(TAG, "tmp: "+value);
            }
        });

   /*    seekBar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               airText.setText(Float.toString(seekBar.getProgress()));
           }
       });
*/

    }
    //實作 Function1使其帶入setProgressChangedCallback
    class Some implements Function1<Float, Unit>{

        private TextView myText;

        Some(TextView airText) {
            myText = airText;
        }

        @Override
        public Unit invoke(Float aFloat) {
            aFloat =  BigDecimal.valueOf(aFloat).setScale(4, BigDecimal.ROUND_HALF_DOWN) .floatValue();
            //seekbar的值從1~0要轉換成16~30
            airText.setText(String.valueOf(((int)(aFloat*14+16)))+"°C");

             Log.d(TAG, "invoke: "+aFloat);
            return null;
        }
    }
}
