package com.upload.service;

import com.common.mongo.MongoService;
import com.upload.domain.FileUploadConfig;

public abstract interface FileUploadConfigService
  extends MongoService<FileUploadConfig>
{}
