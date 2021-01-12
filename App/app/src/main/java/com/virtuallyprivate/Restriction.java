package com.virtuallyprivate;

class Restriction {

    private String packageId;
    private int permissionId;

    public Restriction(String packageId, int permissionId) {
        this.packageId = packageId;
        this.permissionId = permissionId;
    }

    public int getId(DatabaseHelper dbHelper) {
        return dbHelper.getRestrictionPrimaryKey(this.packageId, this.permissionId);
    }

    public String getPackageId() { return packageId; }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }
}
