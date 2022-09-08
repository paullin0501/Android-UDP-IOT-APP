package com.rexlite.rexlitebasicnew;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> fragmentTitle = new ArrayList<>();
    private final List<Integer> mFragmentIconList = new ArrayList<>();
    private Context context;


    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public void add(Fragment fragment, String title, int tabIcon) {
        fragments.add(fragment);
        fragmentTitle.add(title);
        mFragmentIconList.add(tabIcon);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
             /*   Max1Fragment f = new Max1Fragment();
                Bundle args = new Bundle();
                args.putString("deviceId", "14");
                f.setArguments(args);
                return  f;*/
                return new Max1Fragment();
            case 1:
               /* Max1Fragment f2 = new Max1Fragment();
                Bundle args2 = new Bundle();
                args2.putString("deviceId", "16");
                f2.setArguments(args2);
                return f2;*/
                return new Max2Fragment();
            case 2:
               /* Max1Fragment f3 = new Max1Fragment();
                Bundle args3 = new Bundle();
                args3.putString("deviceId", "18");
                f3.setArguments(args3);
                return f3;*/
                return new Max3Fragment();
            case 3:
             /*   Max1Fragment f4 = new Max1Fragment();
                Bundle args4 = new Bundle();
                args4.putString("deviceId", "0b");
                f4.setArguments(args4);*/
                return new SceneFragment();
            case 4:
                return new AirFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }


}
