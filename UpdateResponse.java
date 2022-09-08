package com.rexlite.rexlitebasicnew;

class UpdateResponse {
    String deviceType;
    String version;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public UpdateResponse() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UpdateResponse(String deviceType, String version) {
        this.deviceType = deviceType;
        this.version = version;
    }
}
