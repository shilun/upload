package com.upload.service.utils;

import com.common.exception.ApplicationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
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

    static void executeStream(List<String> result, InputStream inputStream) {
        BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line1 = null;
            while ((line1 = br1.readLine()) != null) {
                if (line1 != null) {
                    result.add(line1);
                }
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
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
            executeStream(result, p.getErrorStream());
            executeStream(result, p.getInputStream());
            p.waitFor();
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