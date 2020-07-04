package com.upload.service.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        Process process = null;
        try {
            log.warn(cmdStr);
            process = Runtime.getRuntime().exec(cmdStr);
            new ProcessClearStream(process.getInputStream(), "INFO").start();
            new ProcessClearStream(process.getErrorStream(), "ERROR").start();
            final Process p = process;
            Callable<Integer> call = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    p.waitFor();
                    return p.exitValue();
                }
            };
            Future<Integer> future = executor.submit(call);
            log.warn("视频分片：=》" + future.get());
        } catch (Exception e) {
            log.error(cmdStr, e);
        } finally {
            if (process == null) {
                process.destroy();
            }
            process = null;
        }
    }

}  
