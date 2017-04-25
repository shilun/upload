package com.neworder.upload.rpc;

import com.common.util.Result;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class UploadUtilTest {
    @Test
    public void testUploadFile() throws IOException {
        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("img.60living.net");
        uploadUtil.setScode("semen");
        uploadUtil.setCode("cbd0262ba9c34b12a2a14022e4c33eb5");
        Result<String> uploadFile = uploadUtil.uploadFile(new File("d:/tt.jpg"));
        System.out.println(uploadFile.getModule());
        byte[] bytes = uploadUtil.downFile("699e88597e564cf29fec965657adfc73.jpg");

    }
}