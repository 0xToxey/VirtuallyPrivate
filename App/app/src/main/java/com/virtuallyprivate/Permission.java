package com.virtuallyprivate;

interface Permissions{
    public static final String CLIPBOARD = "Clipboard";
    public static final String APP_LIST = "App list";
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
