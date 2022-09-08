package com.rexlite.rexlitebasicnew.RoomDataBase;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "DeviceTable")
public class Device {
    @PrimaryKey(autoGenerate = true)//設置是否使ID自動累加
    private int id;
    private String deviceId;
    private String deviceSN;
    private String deviceName;
    private int deviceIcon;
    private boolean isHost;
    private String superior; //deviceSN',不論 max sense, max lite 都會有一個 host //改成M'S用 代表複製的SN
    private String subtitle;

    public Device(int id, String deviceId, String deviceSN, String deviceName, int deviceIcon, boolean isHost, String superior,String subtitle) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceSN = deviceSN;
        this.deviceName = deviceName;
        this.deviceIcon = deviceIcon;
        this.isHost = isHost;
        this.superior = superior;
        this.subtitle = subtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    //建構子
    @Ignore
    public Device(String deviceName, int deviceIcon) {
        this.deviceName = deviceName;
        this.deviceIcon = deviceIcon;
    }
    @Ignore
    public Device(String deviceId, String deviceName, int deviceIcon) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceIcon = deviceIcon;
    }
    @Ignore
    public Device(int deviceIcon) {
        this.deviceIcon = deviceIcon;
    }

    //getter和setter方法


    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public String getSuperior() {
        return superior;
    }

    public void setSuperior(String superior) {
        this.superior = superior;
    }

    public int getDeviceIcon() {
        return deviceIcon;
    }

    public void setDeviceIcon(int deviceIcon) {
        this.deviceIcon = deviceIcon;
    }



    public Device() {
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
