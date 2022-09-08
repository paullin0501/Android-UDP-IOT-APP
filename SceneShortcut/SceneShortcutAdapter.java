package com.rexlite.rexlitebasicnew.SceneShortcut;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rexlite.rexlitebasicnew.ClickListener;
import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;

import java.util.List;

public class SceneShortcutAdapter extends RecyclerView.Adapter<SceneShortcutAdapter.MyViewHolder> {

    private List<Shortcut> shortcutList;
    private ClickListener clickListener;

    SceneShortcutAdapter(List<Shortcut> mShortcutList) {
        this.shortcutList = mShortcutList;
    }

    @Override
    public SceneShortcutAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_shortcut_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SceneShortcutAdapter.MyViewHolder holder, final int position) {
        final Shortcut shortcut = shortcutList.get(position);
        holder.name.setText(shortcut.getName());
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v, shortcut, position);
            }
        });


    }


    @Override
    public int getItemCount() {
        return shortcutList.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        private LinearLayout itemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_text);
            itemLayout = itemView.findViewById(R.id.scene_shortcut_linear);
        }
    }
}
