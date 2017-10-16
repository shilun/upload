package com.upload.service.utils;

import com.common.exception.ApplicationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 控制台处理工具箱
 *
 * @author leizhimin 2009-6-25 14:12:14
 */
public final class CmdToolkit {
    private static Log log = LogFactory.getLog(CmdToolkit.class);

    private CmdToolkit() {
    }

    static void executeStream(List<String> result, InputStream inputStream, CountDownLatch latch) {
        BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line1 = null;
            while ((line1 = br1.readLine()) != null) {
                if (line1 != null) {
                    result.add(line1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        latch.countDown();
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
            final InputStream is1 = p.getInputStream();
            final InputStream is2 = p.getErrorStream();
            final CountDownLatch latch = new CountDownLatch(2);
            new Thread() {
                public void run() {
                    executeStream(result, is1, latch);
                }
            }.start();
            new Thread() {
                public void run() {
                    executeStream(result, is2, latch);
                }
            }.start();
            p.waitFor();
            p.destroy();
            latch.await();
        } catch (Exception e) {
            throw new ApplicationException("exec cmd error");
        }
        return result;
    }
}