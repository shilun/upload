package com.upload.domain;

import com.common.util.AbstractBaseEntity;
import java.io.Serializable;
import java.util.Date;

public class FileUploadConfig
  extends AbstractBaseEntity
  implements Serializable
{
  private String name;
  private String code;
  private String scode;
  private String proposer;
  private String loginName;
  private Integer fileType;
  private Integer httpDown;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getScode() {
    return scode;
  }

  public void setScode(String scode) {
    this.scode = scode;
  }

  public String getProposer() {
    return proposer;
  }

  public void setProposer(String proposer) {
    this.proposer = proposer;
  }

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public Integer getFileType() {
    return fileType;
  }

  public void setFileType(Integer fileType) {
    this.fileType = fileType;
  }

  public Integer getHttpDown() {
    return httpDown;
  }

  public void setHttpDown(Integer httpDown) {
    this.httpDown = httpDown;
  }
}

