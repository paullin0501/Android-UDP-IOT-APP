package com.rexlite.rexlitebasicnew;

class HttpResponse {
    private String device;
    private String version;
    private String release;
    private String change_log;
    private String firmware;
    private String checksum;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getChangeLog() {
        return change_log;
    }

    public void setChangeLog(String changeLog) {
        this.change_log = changeLog;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public HttpResponse() {
    }
}
