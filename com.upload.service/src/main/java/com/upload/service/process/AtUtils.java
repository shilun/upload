package com.upload.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * at任务指令
 */
@Slf4j
@Component
public class AtUtils {
    @Resource
    private ProcessUtil processUtil;
    public void appendFile(String root, String name) {
        if (!root.startsWith("/")) {
            root = "/" + root;
        }
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("mkdir ").append(root).append(name).append(" -p").append("\n");
        String path = root + name;
        builder.append("ffmpeg -i ").append(path).append(".mp4 -ss 1 -f image2  ").append(path).append("/default.jpeg").append("\n");
        builder.append("ffmpeg -i ").append(path).append(".mp4 -metadata rotate=\"\"").append(path).append("/.temp.mp4").append("\n");
        builder.append("ffmpeg -i ").append(path).append("/.temp.mp4 -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list ").append(path).append("/default.m3u8 -segment_time 2 ").append(path).append("/%03d.ts").append("\n");
        builder.append("rm -rf ").append(root).append(name).append(".task");
        builder.append("rm -rf ").append(path).append("/.temp.mp4");
        System.out.println(builder.toString());
        try {
            IOUtils.write(builder.toString().getBytes(), new FileOutputStream(new File(root + name + ".task")));
        } catch (IOException e) {
            log.error("生成任务指令文件失败");
        }

        processUtil.execProcess("at now -f "+path+".task");
    }

}
