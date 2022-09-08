package com.rexlite.rexlitebasicnew;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rexlite.rexlitebasicnew.RoomDataBase.Device;

import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    // group titles
    private List<String> listDataGroup;
    // child data
    private HashMap<String, List<Device>> listDataChild;

    public ExpandableListViewAdapter(Context context, List<String> listDataGroup,
                                     HashMap<String, List<Device>> listChildData) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
//        final String childText = (String) getChild(groupPosition, childPosition);
        Device device = (Device) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.device_list_row_child, null);
        }

        ImageView  childImg = (ImageView) convertView.findViewById(R.id.shortcut_icon);
        TextView textViewChild = convertView.findViewById(R.id.textViewChild);
        TextView idTextView = convertView.findViewById(R.id.idTextView);

       // textViewChild.setText(childText);
        childImg.setImageResource(device.getDeviceIcon());
        textViewChild.setText(device.getDeviceName());
        idTextView.setText("ID: "+device.getDeviceId()+device.getDeviceSN());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataGroup.get(groupPosition))
                .size();
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
