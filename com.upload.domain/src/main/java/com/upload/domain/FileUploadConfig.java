package com.upload.domain;

import com.common.util.AbstractBaseEntity;
import com.upload.domain.model.FileTypeEnum;
import lombok.Data;

import java.io.Serializable;


@Data
public class FileUploadConfig
        extends AbstractBaseEntity
        implements Serializable {
    /**
     * 资源名称
     */
    private String name;
    /**
     * 编码
     */
    private String code;
    /**
     * 短码
     */
    private String scode;
    /**
     * 处理人
     */
    private String proposer;
    private String loginName;
    /**
     * 尺寸
     */
    private String[] sizeinfo;
    /**
     * 类型
     * 1 图片
     * 2 资源
     * 3 视频
     */
    private FileTypeEnum fileType;
    /**
     * 充许http get 下载
     * 1 是
     * 2 否
     */
    private Boolean httpDown;

}

