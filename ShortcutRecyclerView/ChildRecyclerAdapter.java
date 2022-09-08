package com.rexlite.rexlitebasicnew.ShortcutRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rexlite.rexlitebasicnew.R;
import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;

import java.util.List;

class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder> {

    List<Shortcut> items;

    public ChildRecyclerAdapter(List<Shortcut> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.shortcut_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemNameText.setText(items.get(position).getName());
        holder.itemChText.setText(items.get(position).getDeciveCH());
        holder.itemIconView.setImageResource(items.get(position).getIcon());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemNameText;
        TextView itemChText;
        ImageView itemIconView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNameText = itemView.findViewById(R.id.item_name);
            itemChText = itemView.findViewById(R.id.ch_text);
            itemIconView = itemView.findViewById(R.id.shortcut_icon);
        }
    }
}
