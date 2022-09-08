package com.rexlite.rexlitebasicnew;

class SceneConfig {
    private String name;
    private String shortcutType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortcutType() {
        return shortcutType;
    }

    public void setShortcutType(String shortcutType) {
        this.shortcutType = shortcutType;
    }

    public int isShortcut() {
        return isShortcut;
    }

    public void setShortcut(int shortcut) {
        isShortcut = shortcut;
    }

    public SceneConfig() {
    }

    private int isShortcut;
}
