package com.upload.main.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Slf4j
public class FileType {

    private final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();

    private FileType() {
    }

    static {
        getAllFileType();  //初始化文件类型信息  
    }

    private static void getAllFileType() {
        FILE_TYPE_MAP.put("ffd8ff", "jpg");
        FILE_TYPE_MAP.put("89504E47", "png");
        FILE_TYPE_MAP.put("47494638", "gif");
        FILE_TYPE_MAP.put("49492A00", "tif");
        FILE_TYPE_MAP.put("0000002066747970", "mp4");
    }

    /**
     * 得到上传文件的文件头
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null == src || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static String getFileType(File file) {
        String res = null;
        try {
            @SuppressWarnings("resource")
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[10];
            fis.read(b, 0, b.length);
            String fileCode = bytesToHexString(b);
            Iterator<String> keyIter = FILE_TYPE_MAP.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                if (key.toLowerCase().startsWith(fileCode.toLowerCase()) || fileCode.toLowerCase().startsWith(key.toLowerCase())) {
                    res = FILE_TYPE_MAP.get(key);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("获取文件类型失败 message={}", e.getMessage());
        }
        return res;
    }
    /**
     * 获取文件类型
     * @param bytes
     * @return
     */
    public static String getFileType(byte[] bytes) {
        return  FILE_TYPE_MAP.get(bytesToHexString(bytes));
    }


    public static boolean isMp4(byte[] file) {
        byte[] bytes = Arrays.copyOf(file, 10);
        String fileCode = bytesToHexString(bytes);
        if (fileCode.endsWith("667479706d70")) {
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        String fileType = getFileType(new File("/Users/mac/Documents/test.jpg"));
        System.out.println(fileType);
    }

}
