package com.rexlite.rexlitebasicnew;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.rexlite.rexlitebasicnew.RoomDataBase.ExpandDevice;

import java.util.HashMap;
import java.util.List;

public  class ListExpandableViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    // group titles
    private List<String> listDataGroup;
    // child data
    private HashMap<String, List<ExpandDevice>> listDataChild;
    private static final String TAG = "expandList";
    public ListExpandableViewAdapter(Context context, List<String> listDataGroup,
                                     HashMap<String, List<ExpandDevice>>listChildData) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //the first row is used as header
        if (childPosition == 0) {
            //留一個子資料的位置當標題
            convertView = layoutInflater.inflate(R.layout.expandable_header, null);
        }
        if (childPosition>0 ) {
            //取得子項目資料
            ExpandDevice device = (ExpandDevice) getChild(groupPosition, childPosition-1);
            convertView = layoutInflater.inflate(R.layout.device_list_search_row_child, null);



           // ImageView childImg = (ImageView) convertView.findViewById(R.id.search_icon);
            CheckBox childImg = (CheckBox)  convertView.findViewById(R.id.search_icon);
            TextView textViewChild = convertView.findViewById(R.id.nameTextChild);
            TextView subtitleTextViewChild = convertView.findViewById(R.id.subtitleTextChild);
            TextView idTextView = convertView.findViewById(R.id.idTextView);
            ObjectAnimator a = ObjectAnimator.ofInt(textViewChild, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
            ObjectAnimator a1 = ObjectAnimator.ofInt(subtitleTextViewChild, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
            ObjectAnimator a2 = ObjectAnimator.ofInt(idTextView, "textColor", Color.parseColor("#DBDBDB"), Color.parseColor("#F08500"));
            AnimatorSet t = new AnimatorSet();
            AnimatorSet t1 = new AnimatorSet();
            AnimatorSet t2 = new AnimatorSet();
            boolean isShine = listDataChild.get(listDataGroup.get(groupPosition))
                    .get(childPosition-1).isShine();
            Log.d(TAG, "getChildView: isShine"+isShine);
            //播放動畫邏輯
            if(isShine){
                animateText(textViewChild,true,a,t);
                animateText(subtitleTextViewChild,true,a1,t1);
                animateText(idTextView,true,a2,t2);
            }
            if(!isShine){
                animateText(textViewChild,false,a,t);
                animateText(subtitleTextViewChild,false,a1,t1);
                animateText(idTextView,false,a2,t2);
            }
            childImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(childImg.isChecked()) {
                       /* animateText(textViewChild);
                        animateText(subtitleTextViewChild);
                        animateText(idTextView);*/
                        animateText(textViewChild,childImg.isChecked(),a,t);
                        animateText(subtitleTextViewChild,childImg.isChecked(),a1,t1);
                        animateText(idTextView,childImg.isChecked(),a2,t2);
                        listDataChild.get(listDataGroup.get(groupPosition))
                                .get(childPosition-1).setShine(true);
                    }
                    if(!childImg.isChecked()){
                        animateText(textViewChild,false,a,t);
                        animateText(subtitleTextViewChild,false,a1,t1);
                        animateText(idTextView,false,a2,t2);
                        listDataChild.get(listDataGroup.get(groupPosition))
                                .get(childPosition-1).setShine(false);
                    }
                }
            });

            textViewChild.setText(device.getDevice().getDeviceName());
            if (device.getDevice().getSubtitle() != null) {
                subtitleTextViewChild.setText(device.getDevice().getSubtitle());
            } else {
                subtitleTextViewChild.setVisibility(View.GONE);
            }
            idTextView.setText(device.getDevice().getDeviceId() + device.getDevice().getDeviceSN());
        }
            return convertView;

    }

    @SuppressLint("RestrictedApi")
    public void animateText(TextView textViewChild,boolean isShine, ObjectAnimator a,AnimatorSet t ){

        a.setInterpolator(new LinearInterpolator());
        a.setDuration(2000);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setEvaluator(new ArgbEvaluator());

        if(isShine) {
            t.play(a);
            t.start();
        }
        if(!isShine){
           // t.removeAllListeners();
            t.end();
            t.cancel();
            textViewChild.setTextColor(Color.parseColor("#707070"));
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                .size()+1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.device_list_row_group, null);
        }
        TextView textViewGroup = convertView
                .findViewById(R.id.textViewGroup);
        textViewGroup.setTypeface(null, Typeface.BOLD);
        textViewGroup.setText(headerTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
