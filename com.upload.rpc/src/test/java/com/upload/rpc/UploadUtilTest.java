package com.upload.rpc;

import com.common.util.Result;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
//import org.junit.Ignore;
//import org.junit.Test;

import java.io.File;
import java.io.IOException;
public class UploadUtilTest {
//    @Ignore
    @Test
    public void testUploadFile() throws IOException {
        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("localhost");
        uploadUtil.setScode("vedio");
        uploadUtil.setCode("cbd0262ba9c34b12a2a14022e4c33eb4");
        //文件上传
        Result<String> uploadFile = uploadUtil.uploadFile(new File("d:/ss.mp4"));
        System.out.println(uploadFile.getModule());
//        //文件下载
        byte[] bytes = uploadUtil.downFile(uploadFile.getModule());
//
//        //http下载文件
//       String fileUrl="http://img.60community.com/nidone/"+uploadFile.getModule();

    }
}