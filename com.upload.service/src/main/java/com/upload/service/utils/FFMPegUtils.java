package com.upload.service.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FFMPegUtils {

    /***
     * 返回分钟
     * @return
     */
    public static String caculateTime(String path) {
        List<String> strings = CmdToolkit.executeConsole("ffmpeg -i " + path);
        String data = "";
        for (String str : strings) {
            if (str.indexOf("Duration") != -1) {
                data = str.trim().replace("Duration:", "").trim();
                data = data.substring(0, data.indexOf("."));
                break;
            }
        }
        return data;
    }

    /***
     * 返回分钟
     * @return
     */
    public static Integer totalTime(String path) {
        String totalTime = caculateTime(path);
        String[] times = totalTime.split(":");
        Integer time = 0;
        time = NumberUtils.toInt(times[0]) * 60;
        time = time + NumberUtils.toInt(times[1]);
        time = time * 60 + NumberUtils.toInt(times[2]);
        return time;
    }

    public static List<SplitInfo> doSplit(String source) {
        List<SplitInfo> list = new ArrayList<>();
        String totalTime = caculateTime(source);
        String[] times = totalTime.split(":");
        Integer time = 0;
        time = NumberUtils.toInt(times[0]) * 60;
        time = time + NumberUtils.toInt(times[1]);
        time = time * 60 + NumberUtils.toInt(times[2]);
        File file = new File(source);
        int length = (int) (file.length() / 1048576);
        int count = 0;
        if (length % 10 > 0) {
            length = length - length % 10;
            count = length / 10 + 1;
        } else {
            count = length / 10;
        }
        int splitTime = time / count;
        int currentTime = 0;
        while (currentTime < time) {
            SplitInfo info = new SplitInfo();
            int oldCurrent = currentTime;
            info.setStartTime(buildTime(currentTime));
            currentTime = currentTime + splitTime;
            if (currentTime > time) {
                info.setEndTime(buildTime(time));
            } else {
                info.setEndTime(buildTime(currentTime));
            }
            info.setSourceFile(source);
            list.add(info);
        }
        return list;

    }

    public static List<SplitInfo> doSplitDuration(String source) {
        List<SplitInfo> list = new ArrayList<>();
        String totalTime = caculateTime(source);
        String[] times = totalTime.split(":");
        Integer time = 0;
        time = NumberUtils.toInt(times[0]) * 60;
        time = time + NumberUtils.toInt(times[1]);
        time = time * 60 + NumberUtils.toInt(times[2]);
        File file = new File(source);
        int length = (int) (file.length() / 1048576);
        int count = 0;
        if (length % 10 > 0) {
            length = length - length % 10;
            count = length / 10 + 1;
        } else {
            count = 1;
        }
        int splitTime = time / count;
        int currentTime = 0;
        while (currentTime < time) {
            SplitInfo info = new SplitInfo();
            int oldCurrent = currentTime;
            info.setStartTime(buildTime(currentTime));
            currentTime = currentTime + splitTime;
            if (currentTime > time) {
                info.setEndTime(String.valueOf(time - oldCurrent));
            } else {
                info.setEndTime(String.valueOf(splitTime));
            }
            info.setSourceFile(source);
            list.add(info);
        }
        return list;

    }

    private static String buildTime(int time) {
        String content = "{0}:{1}:{2}";
        String hour = "";
        if (time >= 3600) {
            hour = String.valueOf((time - time % 3600) / 3600);
            time = time % 3600;
        }
        String minute = "";
        String second = "";
        if (time >= 60) {
            minute = String.valueOf((time - time % 60) / 60);
            time = time % 60;
            second = String.valueOf(time);
        } else {
            second = String.valueOf(time);
        }
        while (hour.length() < 1) {
            hour = "0" + hour;
        }
        while (minute.length() < 1) {
            minute = "0" + minute;
        }
        while (second.length() < 1) {
            second = "0" + second;
        }
        return MessageFormat.format(content, hour, minute, second);
    }

    private static String timeString(int time) {
        if (time < 10) {
            return "0" + time;
        }
        return String.valueOf(time);
    }

    /**
     * 根据开始时间,结束时间截取
     *
     * @param sourcePath 原视频
     * @param outputPath 输出视频，输出视频的目录必须存在！！！！
     * @param start      开始截取时间
     * @param end        结束时间
     */
    public static void cutWithStartAndEnd(String sourcePath, String outputPath, String start, String end) {

        if (!CommonKit.checkParam(sourcePath, outputPath, start, end)) {
            throw new RuntimeException("参数不能为空");
        }
        if (!CommonKit.fileExist(sourcePath)) {
            throw new RuntimeException("原视频不存在");
        }
//        if (!CommonKit.fileExist(outputPath)) {
//            throw new RuntimeException("输出路径不存在");
//        }
//        if (!CommonKit.checkTime(start)) {
//            throw new RuntimeException("开始时间格式有误");
//        }
//        if (!CommonKit.checkTime(end)) {
//            throw new RuntimeException("结束时间格式有误");
//        }

//        outputPath += (File.separator + "cut_with_start_end.mp4");
//        String command = String.format("-i %s -vcodec copy -acodec copy -ss %s -to %s %s -y -v quiet", sourcePath, start, end, outputPath);
        String command = String.format("ffmpeg -i %s -ss %s -to %s %s -y -v quiet", sourcePath, start, end, outputPath);
        CmdToolkit.executeConsole(command);
    }

    /**
     * 根据开始时间,时长截取
     *
     * @param sourcePath 原视频路径
     * @param outputPath 输出视频，输出视频的目录必须存在！！！！
     * @param start      开始截取时间
     * @param duration   时长
     */
    public static void cutWithDuration(String sourcePath, String outputPath, String start, String duration) {

        if (!CommonKit.checkParam(sourcePath, outputPath, start, duration)) {
            throw new RuntimeException("参数不能为空");
        }
        if (!CommonKit.fileExist(sourcePath)) {
            throw new RuntimeException("原视频不存在");
        }
//        if (!CommonKit.fileExist(outputPath)) {
//            throw new RuntimeException("输出路径不存在");
//        }
//        if (!CommonKit.checkTime(start)) {
//            throw new RuntimeException("开始时间格式有误");
//        }
        if (!CommonKit.checkNumber(duration)) {
            throw new RuntimeException("时长有误");
        }

        //ffmpeg -i JTSJ0090.mp4 -codec copy -acodec copy -ss 4 -t 15 b2.mp4 -y -v quiet
//        String command = String.format("-ss 4 -t %s -accurate_seek -i %s -vcodec copy -acodec copy %s -y -v quiet", duration, sourcePath, outputPath);
        String command = String.format("ffmpeg -i %s -ss %s -t %s %s -y -v quiet", sourcePath, start, duration, outputPath);
        CmdToolkit.executeConsole(command);


    }

    public static boolean split(String path, String file) {
        if(!path.endsWith("/")){
            path=path+"/";
        }
        String[] extFile = file.split("\\.");
        File dirPath = new File(path + extFile[0]);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        String tmu8File = path + extFile[0];
        dirPath = new File(tmu8File);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }

        new File(tmu8File + "/." + extFile[0].substring(1) + ".mp4").deleteOnExit();
        String command = "ffmpeg -i " + path + file + " -metadata rotate='' " + path + extFile[0] + "/." + extFile[0].substring(1) + ".mp4";

//        CmdToolkit.executeConsole(command);
        command = "ffmpeg -i " + tmu8File + "/." + extFile[0].substring(1) + ".mp4 -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list " + tmu8File + "/default.m3u8 -segment_time  5 " + tmu8File + "/%03d.ts";

        CmdToolkit.executeConsole(command);
        new File(tmu8File + "/." + extFile[0].substring(1) + ".mp4").deleteOnExit();
        return true;
    }

    public static void main(String[] args) {
        FFMPegUtils tt=new FFMPegUtils();
        tt.split("/Users/mac/Documents/","ss.mp4");
        System.out.println("fdsa");
    }

    public static void doExportImage(String path, String fileName) {
        String realFile = path + fileName;
        path = path + fileName.substring(0, fileName.indexOf("."));
        File movieDir = new File(path);
        movieDir.mkdirs();
        String command = "ffmpeg -i " + realFile + " -r 1 -t 4 " + path + "/image-%1d.jpeg";
        CmdToolkit.executeConsole(command);
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
