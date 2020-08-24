package com.upload.domain;

import com.common.util.AbstractBaseEntity;
import lombok.Data;

@Data
public class FileInfo extends AbstractBaseEntity {
    private String name;
    private String path;
    private Integer size;
    private Integer execCount;
    private Integer type;
    private Integer status;
    private String videoUrl;
    private String videoImage;
    private String videoSource;
    private Integer hlsStatus;
    public Integer getExecCount() {
        return execCount;
    }
}
