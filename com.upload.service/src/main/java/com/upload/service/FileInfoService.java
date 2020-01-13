package com.upload.service;

import com.common.mongo.MongoService;
import com.upload.domain.FileInfo;
import com.upload.domain.FileUploadConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract interface FileInfoService
  extends MongoService<FileInfo>
{
  public abstract Map<String, Object> downFile(String paramString1, String paramString2);
  
  public abstract byte[] httpDown(String paramString1, String paramString2,String size);
  public abstract File httpDown(String paramString1, String paramString2);
  
  public abstract String upload(MultipartFile paramMultipartFile, String paramString);
  
  public abstract List<String> getPictures();

  public void doVedioSplit();
  public FileUploadConfig findConfigByScode(String scode);
}
