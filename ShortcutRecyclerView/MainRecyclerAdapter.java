package com.rexlite.rexlitebasicnew.ShortcutRecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rexlite.rexlitebasicnew.MainActivity;
import com.rexlite.rexlitebasicnew.Max2SettingActivity;
import com.rexlite.rexlitebasicnew.Max3SettingActivity;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;

import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    List<ShortcutGroup> sectionList;

    public MainRecyclerAdapter(List<ShortcutGroup> sectionList) {
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.shortcut_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ShortcutGroup section = sectionList.get(position);
        String sectionName = section.getTitle();
        Drawable sectionIcon = section.getIcon();
        List<Shortcut> items = section.getShortcutList();
        final Shortcut shortcut = items.get(position);

        holder.titleTextView.setText(sectionName);
        holder.icon.setImageDrawable(sectionIcon);
       // holder.childRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this,3));
        ChildRecyclerAdapter childRecyclerAdapter = new ChildRecyclerAdapter(items);
        holder.childRecyclerView.setAdapter(childRecyclerAdapter);


    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        RecyclerView childRecyclerView;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.title_text);
            icon = itemView.findViewById(R.id.title_icon);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
