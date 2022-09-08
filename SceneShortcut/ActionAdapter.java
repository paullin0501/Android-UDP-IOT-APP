package com.rexlite.rexlitebasicnew.SceneShortcut;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rexlite.rexlitebasicnew.ClickListener;
import com.rexlite.rexlitebasicnew.R;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.MyViewHolder> {

    private String[] actions;
    private ClickListener clickListener;

    ActionAdapter(String[] mActions){
        this.actions = mActions;
    }

    @Override
    public ActionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_shortcut_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActionAdapter.MyViewHolder holder, final int position) {
        final String action = actions[position];
        holder.name.setText(action);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v,action,position);
            }
        });


    }


    @Override
    public int getItemCount() {
        return actions.length;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        private LinearLayout itemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_text);
            itemLayout =  itemView.findViewById(R.id.scene_shortcut_linear);
        }
    }
}
