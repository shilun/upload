package com.upload.domain;

import com.common.util.AbstractBaseEntity;

public class FileInfo extends AbstractBaseEntity {
    private String name;
    private String path;
    private Integer size;
    private Integer execCount;
    private Integer type;
    private Integer status;
    public Integer getExecCount() {
        return execCount;
    }

    public void setExecCount(Integer execCount) {
        this.execCount = execCount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    private Integer maxExecCount;

    public Integer getMaxExecCount() {
        return maxExecCount;
    }

    public void setMaxExecCount(Integer maxExecCount) {
        this.maxExecCount = maxExecCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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
