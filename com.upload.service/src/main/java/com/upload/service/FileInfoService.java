package com.upload.service;

import com.common.util.AbstractBaseService;
import com.upload.domain.FileInfo;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public abstract interface FileInfoService
  extends AbstractBaseService<FileInfo>
{
  public abstract Map<String, Object> downFile(String paramString1, String paramString2);
  
  public abstract byte[] httpDown(String paramString1, String paramString2,String size);
  public abstract File httpDown(String paramString1, String paramString2);
  
  public abstract String upload(MultipartFile paramMultipartFile, String paramString);
  
  public abstract List<String> getPictures();
}
