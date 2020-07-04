package com.upload.service.utils;

import com.common.exception.ApplicationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

/**
 * 控制台处理工具箱
 *
 * @author leizhimin 2009-6-25 14:12:14
 */
public final class CmdToolkit {
    private static Log log = LogFactory.getLog(CmdToolkit.class);

    private CmdToolkit() {
    }



    public static void dealStream(Process process) {
        // 处理InputStream的线程
        new Thread() {
            @Override
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                try {
                    while ((line = in.readLine()) != null) {
                        log.info("output: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        // 处理ErrorStream的线程
        new Thread() {
            @Override
            public void run() {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = null;
                try {
                    while ((line = err.readLine()) != null) {
                        log.info(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        err.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    /**
     * 读取控制命令的输出结果
     *
     * @param cmd 命令
     * @return 控制命令的输出结果
     * @throws IOException
     */
    public static List<String> executeConsole(String cmd) {
        final List<String> result = new ArrayList<String>();
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            dealStream(p);
            int i = p.exitValue();
            if (i != 0) {
                throw new ApplicationException("");
            }
        } catch (Exception e) {
            throw new ApplicationException("exec cmd error", e);
        } finally {
            try {
                p.destroy();
            } catch (Exception e) {
                log.error("执行命令行失败");
            }
        }
        return result;
    }


    public static String toString(OutputStream out) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream.toString();
    }
}
