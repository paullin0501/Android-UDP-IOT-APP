package com.rexlite.rexlitebasicnew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

public class DeviceActivity extends AppCompatActivity {
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        int[] imageResId = {
                R.drawable.max1,
                R.drawable.max2,
                R.drawable.max3 };

        viewPager = findViewById(R.id.viewpager);

        //set up adapter
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // add fragments(max1 max2 max3 scene air)
        viewPagerAdapter.add(new Max1Fragment(),"Max 1",imageResId[0]);
        viewPagerAdapter.add(new Max2Fragment(),"Max 2",imageResId[1]);
        viewPagerAdapter.add(new Max3Fragment(),"Max 3",imageResId[2]);

        //set adapter

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout = findViewById(R.id.tab_layout);
       /* for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(viewPagerAdapter.getTabView(i));
        }*/
        tabLayout.setupWithViewPager(viewPager);


        for (int i = 0; i < imageResId.length; i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }

        /*View view1 = getLayoutInflater().inflate(R.layout.customtab, null);
        view1.findViewById(R.id.tabImageView).setBackgroundResource(R.drawable.max1);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));


        View view2 = getLayoutInflater().inflate(R.layout.customtab, null);
        view2.findViewById(R.id.tabImageView).setBackgroundResource(R.drawable.max2);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));


        View view3 = getLayoutInflater().inflate(R.layout.customtab, null);
        view3.findViewById(R.id.tabImageView).setBackgroundResource(R.drawable.max3);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));*/


    }

   /* private View getview(int icon) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.customtab, null);
        ImageView iv = view.findViewById(R.id.iv);
        iv.setImageResource(icon);
        return view;*/
    }


