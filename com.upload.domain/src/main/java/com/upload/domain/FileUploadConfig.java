package com.upload.domain;

import com.common.util.AbstractBaseEntity;
import java.io.Serializable;
import java.util.Date;

public class FileUploadConfig
  extends AbstractBaseEntity
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private Long id;
  private String name;
  private String code;
  private String scode;
  private String proposer;
  private String loginName;
  private Integer fileType;
  private Date createTime;
  private Date updateTime;
  private Integer httpDown;

  public Integer getHttpDown()
  {
    return this.httpDown;
  }

  public void setHttpDown(Integer httpDown)
  {
    this.httpDown = httpDown;
  }

  public Long getId()
  {
    return this.id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getCode()
  {
    return this.code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getScode()
  {
    return this.scode;
  }

  public void setScode(String scode)
  {
    this.scode = scode;
  }

  public String getProposer()
  {
    return this.proposer;
  }

  public void setProposer(String proposer)
  {
    this.proposer = proposer;
  }

  public String getLoginName()
  {
    return this.loginName;
  }

  public void setLoginName(String loginName)
  {
    this.loginName = loginName;
  }

  public Integer getFileType()
  {
    return this.fileType;
  }

  public void setFileType(Integer fileType)
  {
    this.fileType = fileType;
  }

  public Date getCreateTime()
  {
    return this.createTime;
  }

  public void setCreateTime(Date createTime)
  {
    this.createTime = createTime;
  }

  public Date getUpdateTime()
  {
    return this.updateTime;
  }

  public void setUpdateTime(Date updateTime)
  {
    this.updateTime = updateTime;
  }
}

