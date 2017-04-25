package com.upload.dao.impl;

import com.common.util.DefaultBaseDao;
import com.upload.dao.FileUploadConfigDao;
import com.upload.domain.FileUploadConfig;
import org.springframework.stereotype.Component;

@Component
public class FileUploadConfigDaoImpl
        extends DefaultBaseDao<FileUploadConfig>
        implements FileUploadConfigDao
{
    private static final long serialVersionUID = 1L;
    private static final String NAMESPACE = "com.upload.dao.FileUploadConfigDao.";

    public String getNameSpace(String statement)
    {
        return "com.upload.dao.FileUploadConfigDao." + statement;
    }
}
