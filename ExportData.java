package com.rexlite.rexlitebasicnew;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ExportData {
    private String host = "Android";
    private int cnt;
    private Map<String, ExportDevice> device = new HashMap<String, ExportDevice>();

    public Map<String, ExportDevice> getDevice() {
        return device;
    }

    public void setDevice(Map<String, ExportDevice> device) {
        this.device = device;
    }

    public ExportData() {
    }


    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }



    public ExportData(int deviceClass, int cnt) {
        this.cnt = cnt;


    }
}
