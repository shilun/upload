package com.upload.dao.impl;

import com.common.util.DefaultBaseDao;
import com.upload.dao.FileInfoDao;
import com.upload.domain.FileInfo;
import org.springframework.stereotype.Component;

@Component
public class FileInfoDaoImpl
        extends DefaultBaseDao<FileInfo>
        implements FileInfoDao
{
    private static final long serialVersionUID = 1L;
    private static final String NAMESPACE = "com.upload.dao.FileInfoDao.";

    public String getNameSpace(String statement)
    {
        return "com.upload.dao.FileInfoDao." + statement;
    }
}
