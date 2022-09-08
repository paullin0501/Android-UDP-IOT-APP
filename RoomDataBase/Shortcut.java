package com.rexlite.rexlitebasicnew.RoomDataBase;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "ShortcutTable")
public class Shortcut {
    @PrimaryKey(autoGenerate = true)//設置是否使ID自動累加
    private int id;
    private String name;
    private int icon;
    private  String deviceType; //分成device跟scene
    private String hostDeviceSN; //哪個設備控制的
    private String deciveCH;
    private boolean isShow; //是否顯示在主頁
    private String hostDeviceName; //裝置的名稱

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type; //裝置種類

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public Shortcut(int id, String name, int icon, String deviceType, String hostDeviceSN, String deciveCH, boolean isShow,String hostDeviceName) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.deviceType = deviceType;
        this.hostDeviceSN = hostDeviceSN;
        this.deciveCH = deciveCH;
        this.isShow = isShow;
        this.hostDeviceName = hostDeviceName;
    }

    public String getHostDeviceName() {
        return hostDeviceName;
    }

    public void setHostDeviceName(String hostDeviceName) {
        this.hostDeviceName = hostDeviceName;
    }

    @Ignore
    public Shortcut() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getHostDeviceSN() {
        return hostDeviceSN;
    }

    public void setHostDeviceSN(String hostDeviceSN) {
        this.hostDeviceSN = hostDeviceSN;
    }

    public String getDeciveCH() {
        return deciveCH;
    }

    public void setDeciveCH(String deciveCH) {
        this.deciveCH = deciveCH;
    }
}
