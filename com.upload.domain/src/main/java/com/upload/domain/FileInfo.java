package com.upload.domain;

import com.common.util.AbstractBaseEntity;

public class FileInfo extends AbstractBaseEntity {
    private String name;
    private String path;
    private Integer size;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
