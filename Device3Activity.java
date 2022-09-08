package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Device3Activity extends AppCompatActivity {
    private ViewPager viewPager;
    FragmentTransaction fragmentTransaction;

    final UpdateDialog loadingDialog = new UpdateDialog(Device3Activity.this);
    private boolean finishUpdate = false;
    private Button btnNavFrag1;
    private Button btnNavFrag2;
    private Button btnNavFrag3;
    private Button btnNavFrag4;
    private Button btnNavFrag5;
    public TextView title;
    private static final String TAG = "device3";
    @Override
    public void onStart() {
        super.onStart();
        // 在此Activity啟用EventBus
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    // 註冊Subscribe
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateStatus event) {
        Log.d(TAG, "onMessageEvent: " + event.getUpgrade());
        if(event.getUpgrade().equals("0") && finishUpdate){
            loadingDialog.dismissDialog();
            finishUpdate = false;
        }
        if(event.getUpgrade().equals("1")){
            loadingDialog.startLoadingDialog();
            loadingDialog.setLoadingText("Firmware is updating...");
            finishUpdate = true;
        }
        //根據mqtt的回傳值來顯示更新的UI


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device3);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        title = findViewById(R.id.toolbar_title);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        int id = getIntent().getIntExtra("id", 0);
        PageChangeListenerImpl listener = new PageChangeListenerImpl();
        //監聽滑動事件
        viewPager.addOnPageChangeListener(listener);
        //限制重新創建adapter的頁面數
        viewPager.setOffscreenPageLimit(3);
        title.setText("MAXLiTE1");
        //返回按鍵跳轉回來時顯示相對應的fragment
        if (id == 1) {
            setViewPager(0);
            title.setText("MAXLiTE1");
        }
        if (id == 2) {
            setViewPager(1);
            title.setText("MAXLiTE2");
        }
        if (id == 3) {
            setViewPager(2);
            title.setText("MAXLiTE3");
        }
        if (id == 4) {
            setViewPager(3);
            title.setText("MAXScene");
        }
        ImageView leftIcon = findViewById(R.id.left_icon);
           leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Device3Activity.this,MainActivity.class);
                intent.putExtra("resume",true);
                startActivity(intent);
                Animatoo.animateSlideRight(Device3Activity.this);
            }
        });
        btnNavFrag1 = findViewById(R.id.max1);
        btnNavFrag2 = findViewById(R.id.max2);
        btnNavFrag3 = findViewById(R.id.max3);
        btnNavFrag4 = findViewById(R.id.scene);
        btnNavFrag5 = findViewById(R.id.air);

        btnNavFrag1.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 1", Toast.LENGTH_SHORT).show();
                setViewPager(0);
                title.setText("MAXLiTE1");

            }

        });

        btnNavFrag2.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                //  Toast.makeText(getActivity(), "Going to Fragment 2", Toast.LENGTH_SHORT).show();
                setViewPager(1);
                title.setText("MAXLiTE2");

            }

        });

        btnNavFrag3.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                setViewPager(2);
                title.setText("MAXLiTE3");

            }

        });
        btnNavFrag4.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                setViewPager(3);
                title.setText("MAXScene");
            }

        });
        btnNavFrag5.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                setViewPager(4);
                title.setText("MAXAir");

            }

        });
     /*   DeviceMenuFragment frag = new DeviceMenuFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, frag);
        fragmentTransaction.commit();*/

    }

    //覆寫手機預設的返回鍵 避免被功能選單的fragment返回鍵搞混返回的順序 才可以確實回到MainActivity
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("resume", true);
        startActivity(intent);
        Animatoo.animateSlideRight(this);
    }

    /*public void onClick(View view) {
        transaction = manager.beginTransaction();
        switch (view.getId()) {
            case R.id.max1:
                Max1Fragment fragment1 = new Max1Fragment();
                Log.d("test", "onClick: "+1);
                transaction.replace(R.id.center, fragment1, "fragment1");
                break;
            case R.id.max2:
                Max2Fragment fragment2 = new Max2Fragment();
                Log.d("test", "onClick: "+2);
                transaction.replace(R.id.center, fragment2, "fragment2");
                break;
            case R.id.max3:
                Max2Fragment fragment3 = new Max2Fragment();
                transaction.replace(R.id.center, fragment3, "fragment3");
                break;

        }

        transaction.commit();
    }*/
    public void setViewPager(int fragmentNumber) {
        viewPager.setCurrentItem(fragmentNumber);
    }

    public class PageChangeListenerImpl implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int selected) {
            if (selected == 0) {
                setViewPager(0);
                title.setText("MAXLiTE1");
            }
            if (selected== 1) {
                setViewPager(1);
                title.setText("MAXLiTE2");
            }
            if (selected== 2) {
                setViewPager(2);
                title.setText("MAXLiTE3");
            }
            if (selected == 3) {
                setViewPager(3);
                title.setText("MAXScene");
            }
            if (selected == 4) {
                setViewPager(4);
                title.setText("MAXAir");
            }
            Log.d(TAG, "onPageSelected: "+selected);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }
    }
}
