package com.upload.service.impl;

import com.common.exception.BizException;
import com.common.mongo.AbstractMongoService;
import com.upload.domain.FileUploadConfig;
import com.upload.service.FileUploadConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
    public class FileUploadConfigServiceImpl  extends AbstractMongoService<FileUploadConfig>    implements FileUploadConfigService {
    private static final long serialVersionUID = 1L;

    @Override
    protected Class getEntityClass() {
        return FileUploadConfig.class;
    }

    public Long save(FileUploadConfig config) {
        if (config.getId() != null) {
            config.setScode(null);
            config.setCode(null);
            config.setLoginName(null);
        } else {
            FileUploadConfig query = new FileUploadConfig();
            query.setScode(config.getScode());
            Long count = queryCount(query);
            if (count > 0) {
                throw new BizException("简码重复");
            }
            if (StringUtils.isBlank(config.getLoginName())) {
                throw new BizException("经办人为空！");
            }
            if (StringUtils.isBlank(config.getScode())) {
                throw new BizException("简码为空！");
            }
            if (StringUtils.isBlank(config.getName())) {
                throw new BizException("业务名称为空！");
            }
            if (StringUtils.isBlank(config.getCode())) {
                throw new BizException("业务码为空！");
            }
            if (config.getFileType() == null) {
                throw new BizException("文件类型不能为空！");
            }
        }
        return super.save(config);
    }
}
