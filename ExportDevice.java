package com.rexlite.rexlitebasicnew;

import java.util.HashMap;
import java.util.Map;

class ExportDevice {
    private String name;
    private String subtitle ="";
    private int devClass;
    private String devType;
    private String sn;
    private Map<String, ChannelConfig> channelConfig ;
    private Map<String, SceneConfig> sceneConfig ;



    public Map<String, SceneConfig> getSceneConfig() {
        return sceneConfig;
    }

    public void setSceneConfig(Map<String, SceneConfig> sceneConfig) {
        this.sceneConfig = sceneConfig;
    }

    public void setChannelConfig(Map<String, ChannelConfig> channelConfig) {
        this.channelConfig = channelConfig;
    }

    public Map<String, ChannelConfig> getChannelConfig() {
        return channelConfig;
    }

    public ExportDevice() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getDevClass() {
        return devClass;
    }

    public void setDevClass(int devClass) {
        this.devClass = devClass;
    }

    public String getType() {
        return devType;
    }

    public void setType(String type) {
        this.devType = type;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

}
