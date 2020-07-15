package com.upload.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author xdwang
 * @ceate 2012-7-20 下午22:22:44
 * @email xdwangiflytek@gmail.com
 * @description process工具类
 */
@Slf4j
@Component
public class ProcessUtil {

    ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * @param cmdStr 命令字符串
     * @descrption 执行外部exe公用方法
     * @author xdwang
     * @create 2012-7-20下午22:24:32
     */
    public void execProcess(String cmdStr) {
        ArrayList<String> commonds = new ArrayList<>();
        commonds.add(cmdStr);
        String s = executeCommand(commonds);
    }



    /**
     * 执行FFmpeg命令
     * @param commonds 要执行的FFmpeg命令
     * @return FFmpeg程序在执行命令过程中产生的各信息，执行出错时返回null
     */
    public static String executeCommand(List<String> commonds) {
        if (CollectionUtils.isEmpty(commonds)) {
            log.error("--- 指令执行失败，因为要执行的FFmpeg指令为空！ ---");
            return null;
        }
        LinkedList<String> ffmpegCmds = new LinkedList<>(commonds);
        log.info("--- 待执行的FFmpeg指令为：---" + ffmpegCmds);

        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = null;
        try {
            // 执行ffmpeg指令
//            ProcessBuilder builder = new ProcessBuilder();
//            builder.command(ffmpegCmds);
            ffmpeg=runtime.exec(commonds.get(0));
//            ffmpeg = builder.start();
//            log.info("--- 开始执行FFmpeg指令：--- 执行线程名：" + builder.toString());

            // 取出输出流和错误流的信息
            // 注意：必须要取出ffmpeg在执行命令过程中产生的输出信息，如果不取的话当输出流信息填满jvm存储输出留信息的缓冲区时，线程就回阻塞住
            PrintStream errorStream = new PrintStream(ffmpeg.getErrorStream());
            PrintStream inputStream = new PrintStream(ffmpeg.getInputStream());
            errorStream.start();
            inputStream.start();
            // 等待ffmpeg命令执行完
            ffmpeg.waitFor();

            // 获取执行结果字符串
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer).toString();

            // 输出执行的命令信息
            String cmdStr = Arrays.toString(ffmpegCmds.toArray()).replace(",", "");
            String resultStr = StringUtils.isBlank(result) ? "【异常】" : "正常";
            log.info("--- 已执行的FFmepg命令： ---" + cmdStr + " 已执行完毕,执行结果： " + resultStr);
            return result;

        } catch (Exception e) {
            log.error("--- FFmpeg命令执行出错！ --- 出错信息： " + e.getMessage());
            return null;

        } finally {
            if (null != ffmpeg) {
                ProcessKiller ffmpegKiller = new ProcessKiller(ffmpeg);
                // JVM退出时，先通过钩子关闭FFmepg进程
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

}
/**
 * 在程序退出前结束已有的FFmpeg进程
 */
@Slf4j
class ProcessKiller extends Thread {
    private Process process;

    public ProcessKiller(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        this.process.destroy();
        log.info("--- 已销毁FFmpeg进程 --- 进程名： " + process.toString());
    }
}
/**
 * 用于取出ffmpeg线程执行过程中产生的各种输出和错误流的信息
 */
@Slf4j
class PrintStream extends Thread {
    InputStream inputStream = null;
    BufferedReader bufferedReader = null;
    StringBuffer stringBuffer = new StringBuffer();

    public PrintStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            if (null == inputStream) {
                log.error("--- 读取输出流出错！因为当前输出流为空！---");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            log.error("--- 读取输入流出错了！--- 错误信息：" + e.getMessage());
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("--- 调用PrintStream读取输出流后，关闭流时出错！---");
            }
        }
    }
}
