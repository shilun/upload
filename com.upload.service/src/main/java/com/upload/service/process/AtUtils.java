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
        File taskDir = new File(root + "/task/");
        if (!taskDir.exists()) {
            taskDir.mkdir();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("eval \"mkdir ").append(root).append(name).append(" -p").append("\n\"");
        String path = root + name;
        builder.append("eval \"ffmpeg -i ").append(path).append(".mp4 -r 1 -t 1  ").append(path).append("/default.jpeg").append("\n\"");
        builder.append("eval \"ffmpeg -i ").append(path).append(".mp4 -metadata rotate=\"\" ").append(path).append("/temp.mp4").append("\n\"");
        builder.append("eval \"ffmpeg -i ").append(path).append("/temp.mp4 -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list ").append(path).append("/default.m3u8 -segment_time 2 ").append(path).append("/%03d.ts").append("\n\"");
        builder.append("eval \"rm -rf ").append(root).append("task/").append(name).append(".task").append("\n\"");
        builder.append("eval \"rm -rf ").append(path).append("/temp.mp4\"");
        try {
            IOUtils.write(builder.toString().getBytes(), new FileOutputStream(new File(root + "task/" + name + ".task")));
        } catch (IOException e) {
            log.error("生成任务指令文件失败");
        }

        processUtil.execProcess("at now -M -f " + root + "task/" + name + ".task");
    }

    public void reDoTaskFile(String root) {
        File file = new File(root);
        File[] files = file.listFiles();
        long currentDate = System.currentTimeMillis() - 30 * 1000;
        long errorTime = System.currentTimeMillis() - 60 * 1000;

        for (File fileItem : files) {
            long l = fileItem.lastModified();
            if (l < errorTime) {
                log.error("视频出错 文件=>{}", fileItem.getAbsoluteFile());
                fileItem.delete();
                continue;
            }
            if (l < currentDate) {
                processUtil.execProcess("at now -f " + fileItem.getAbsolutePath());
            }
        }
    }
}
