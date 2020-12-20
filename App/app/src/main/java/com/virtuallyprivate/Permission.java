package com.virtuallyprivate;

interface Permissions{
    String CLIPBOARD = "Clipboard";
    String APP_LIST = "App list";
    String LOCATION = "Location";
    String MIC = "Microphone";
}

class Permission {

    private String name;

    public Permission(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                '}';
    }

    public int getId(DatabaseManager dbManager) {
        return dbManager.getPermissionPrimaryKey(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
