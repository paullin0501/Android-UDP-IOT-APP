package com.rexlite.rexlitebasicnew.ShortcutRecyclerView;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.rexlite.rexlitebasicnew.RoomDataBase.Shortcut;

import java.util.List;

 public  class ShortcutGroup {
    public  String title;
    public  Drawable icon;
    public  List<Shortcut> shortcutList;

     public String getTitle() {
         return title;
     }

     public void setTitle(String title) {
         this.title = title;
     }

     public Drawable getIcon() {
         return icon;
     }

     public void setIcon(Drawable icon) {
         this.icon = icon;
     }

     public List<Shortcut> getShortcutList() {
         return shortcutList;
     }

     public void setShortcutList(List<Shortcut> shortcutList) {
         this.shortcutList = shortcutList;
     }

     public ShortcutGroup(String title, Drawable icon, List<Shortcut> shortcutList) {
        this.title = title;
        this.icon = icon;
        this.shortcutList = shortcutList;
    }
}
