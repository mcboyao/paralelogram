package com.paralelogram.user.model;

public enum RoleRequest {

    ADMIN ("paralelogram_admin"),
    VISITOR ("paralelogram_visitor");

    private String value;

    RoleRequest(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
