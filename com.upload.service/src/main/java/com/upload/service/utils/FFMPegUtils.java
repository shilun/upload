package com.upload.service.utils;

import com.common.exception.ApplicationException;
import com.common.httpclient.HttpClientUtil;
import com.upload.service.process.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.stereotype.Component;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collection;

@Component
@Slf4j
public class FFMPegUtils {

    @Resource
    private ProcessUtil processUtil;

    public long length(String file) {
        MultimediaObject instance = new MultimediaObject(new File(file));
        MultimediaInfo result = null;
        try {
            result = instance.getInfo();
        } catch (EncoderException e) {
            log.error("read.mp4.time.error message={}", e.getMessage());
            throw new ApplicationException("read.mp4.time.error");

        }
        return result.getDuration() / 1000;
    }



    public void doExportImage(String path, String fileName) {
        String realFile = path + fileName;
        path = path + fileName.substring(0, fileName.indexOf("."));
        File movieDir = new File(path);
        movieDir.mkdirs();
        String command = "ffmpeg -i " + realFile + " -r 1 -t 4 " + path + "/image-%1d.jpeg";
//        CmdToolkit.executeConsole(command);
        processUtil.execProcess(command);
        Collection<File> files = FileUtils.listFiles(new File(path), FileFilterUtils.suffixFileFilter("jpeg"), null);
        File maxFile = null;
        for (File item : files) {
            if (maxFile == null) {
                maxFile = item;
            }
            if (item.length() > maxFile.length()) {
                maxFile = item;
            }
        }
        for (File item : files) {
            if (item != maxFile) {
                item.delete();
            }
        }
        maxFile.renameTo(new File(maxFile.getParentFile().getPath() + "/default.jpeg"));
    }

}
