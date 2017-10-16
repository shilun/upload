 package com.upload.domain.model;

 import com.common.util.IGlossary;

 public enum FileTypeEnum
   implements IGlossary
 {
   ORDINARYFILE("普通文件", Integer.valueOf(1))
     ,
     PICTURE("图片", Integer.valueOf(2)),
     VEDIO("视频", Integer.valueOf(3));

   private String name;
   private Integer value;

   private FileTypeEnum(String name, Integer value)
   {
     this.name = name;
     this.value = value;
   }

   public String getName()
   {
     return this.name;
   }

   public Integer getValue()
   {
     return this.value;
   }
 }

