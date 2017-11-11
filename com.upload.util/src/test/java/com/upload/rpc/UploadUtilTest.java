package com.upload.rpc;

import com.common.util.Result;
import com.common.util.StringUtils;
import org.junit.Test;
//import org.junit.Ignore;
//import org.junit.Test;

import java.io.File;
import java.io.IOException;
public class UploadUtilTest {
//    @Ignore
    @Test
    public void testUploadFile() throws IOException {

        System.out.println(StringUtils.getUUID());
        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("localhost");
        uploadUtil.setScode("video");
        uploadUtil.setCode("cbd0262ba9c34b12a2a14022e4c33eb8");
        //文件上传
        Result<String> uploadFile = uploadUtil.uploadFile(new File("d:/ss.mp4"));
        System.out.println(uploadFile.getModule());
//        //文件下载
        byte[] bytes = uploadUtil.downFile(uploadFile.getModule());

        //http下载文件
       String fileUrl="http://img.60community.com/video/"+uploadFile.getModule();

    }
}