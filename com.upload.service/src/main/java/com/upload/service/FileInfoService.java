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
  
  public abstract String upload(MultipartFile paramMultipartFile, String key,FileUploadConfig config);
  
  /**
   * 根据key查找业务
   * @param key
   * @return
   */
  public FileUploadConfig findConfigByKey(String key);

  /**
   * 根据短码获取业务
   * @param scode
   * @return
   */
  public FileUploadConfig findConfigByScode(String scode);


  /**
   * 根据视频id查找文件
   * @param voideId
   * @return
   */
  public FileInfo findVideoByVideoId(String voideId);
}
