package com.rexlite.rexlitebasicnew.RoomDataBase;

//為了避免expandable view 按按鈕刷新 新創的類別
public class ExpandDevice {
    private boolean isShine = false;
    private Device device;

    public ExpandDevice(boolean isShine, Device device) {
        this.isShine = isShine;
        this.device = device;
    }

    public boolean isShine() {
        return isShine;
    }

    public void setShine(boolean shine) {
        isShine = shine;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
