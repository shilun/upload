package com.upload.service.process;

import com.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     *
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
        PrintStream inputStream = null;
        PrintStream errorStream = null;
        Process ffmpeg = null;
        CountDownLatch countDownLatch=new CountDownLatch(2);
        try {
            ffmpeg = runtime.exec(commonds.get(0));
            errorStream = new PrintStream(ffmpeg.getErrorStream(),countDownLatch);
            inputStream = new PrintStream(ffmpeg.getInputStream(),countDownLatch);
            errorStream.start();
            inputStream.start();
            countDownLatch.await();
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
            throw new ApplicationException("ffmpeg.error");

        } finally {

            ffmpeg.destroy();
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
    CountDownLatch downLatch;

    public PrintStream(InputStream inputStream, CountDownLatch downLatch) {
        this.inputStream = inputStream;
        this.downLatch = downLatch;
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
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(inputStream);
            downLatch.countDown();
        }
    }
}
