package com.upload.rpc;

import java.io.Serializable;
import java.util.List;

/**
 * 文件信息
 */
public class FileInfo implements Serializable {
    /**
     * 名称
     */
    private String name;
    /**
     *
     */
    private String realName;
    /**
     * 文件大小
     */
    private Integer fileSize;
    /***
     * 子文件数
     */
    private List<FileInfo> items;
    /***
     * 1 普通文件
     * 2 图片
     * 3 视频
     */
    private Integer type;
    /**
     * 时长
     */
    private Integer time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public List<FileInfo> getItems() {
        return items;
    }

    public void setItems(List<FileInfo> items) {
        this.items = items;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
