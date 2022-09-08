package com.rexlite.rexlitebasicnew;

import java.util.List;

class ChannelConfig {
   private String name;
   private String devType;
   private int isShortcut;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public int isShortcut() {
        return isShortcut;
    }

    public void setShortcut(int shortcut) {
        isShortcut = shortcut;
    }

    public ChannelConfig() {
    }
}
