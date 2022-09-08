package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class TestAnimActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_anim);
    }

    public void mainBtn(View view) {
        if(view == findViewById(R.id.windmillBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateWindmill(this);
        }
        if(view == findViewById(R.id.spinBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSpin(this);
        }
        if(view == findViewById(R.id.diagonalBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateDiagonal(this);
        }

        if(view == findViewById(R.id.splitBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSplit(this);
        }
        if(view == findViewById(R.id.shrinkBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateShrink(this);
        }
        if(view == findViewById(R.id.cardBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateCard(this);
        }
        if(view == findViewById(R.id.inandoutBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateInAndOut(this);
        }
        if(view == findViewById(R.id.swipeleftBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSwipeLeft(this);
        }
        if(view == findViewById(R.id.swiperightBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSwipeRight(this);
        }
        if(view == findViewById(R.id.slideleftBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSlideLeft(this);
        }
        if(view == findViewById(R.id.sliderightBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSlideRight(this);
        }
        if(view == findViewById(R.id.slideupBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSlideUp(this);
        }
        if(view == findViewById(R.id.slidedownBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateSlideDown(this);
        }
        if(view == findViewById(R.id.zoomBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateZoom(this);
        }
        if(view == findViewById(R.id.fadeBtn)){
            startActivity(new Intent(this,TestNewActivity.class));
            Animatoo.animateFade(this);
        }
    }
}