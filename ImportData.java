package com.rexlite.rexlitebasicnew;

import java.util.HashMap;
import java.util.Map;

class ImportData {
    private String host;
    private String cmd;
    private int cnt;
    private Map<String, ExportDevice> device = new HashMap<String, ExportDevice>();
    private FindDevice findDevice;

    public FindDevice getFindDevice() {
        return findDevice;
    }

    public void setFindDevice(FindDevice findDevice) {
        this.findDevice = findDevice;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public Map<String, ExportDevice> getDevice() {
        return device;
    }

    public void setDevice(Map<String, ExportDevice> device) {
        this.device = device;
    }

    public ImportData() {
    }
}
