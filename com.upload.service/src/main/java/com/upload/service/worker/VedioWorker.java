package com.upload.service.worker;

import com.common.util.model.YesOrNoEnum;
import com.upload.domain.FileInfo;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.utils.FFMPegUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class VedioWorker {
    private final static Logger logger = LoggerFactory.getLogger(VedioWorker.class);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
    @Resource
    private FileInfoService fileInfoService;
    @Value("${app.fileRootPath}")
    private String fileRootPath;
    @Value("${app.domain}")
    private String appDomain;
    private volatile AtomicBoolean splitRunning = new AtomicBoolean(false);

    private volatile AtomicBoolean indexRunnint = new AtomicBoolean(false);

    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60 + 1)
    public void execute() {
        if (!splitRunning.getAndSet(true)) {
            FileInfo query = new FileInfo();
            query.setType(FileTypeEnum.VIDEO.getValue());
            query.setStatus(YesOrNoEnum.NO.getValue());
            query.setDelStatus(YesOrNoEnum.NO.getValue());
            query.setMaxExecCount(3);
            List<FileInfo> query1 = fileInfoService.query(query);
            for (FileInfo item : query1) {
                doVideoFile(item);
            }
            splitRunning.set(false);
        }
    }

    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60 + 1)
    public void executeIndexFile() {
        if (!indexRunnint.getAndSet(true)) {
            FileInfo query = new FileInfo();
            query.setType(FileTypeEnum.VIDEO.getValue());
            query.setHlsStatus(YesOrNoEnum.NO.getValue());
            query.setMaxExecCount(3);
            List<FileInfo> query1 = fileInfoService.query(query);
            for (FileInfo item : query1) {
                doExecuteIndexFile(item);
            }
            indexRunnint.set(false);
        }
    }

    public void doExecuteIndexFile(final FileInfo item) {
        if (!fileRootPath.endsWith("/")) {
            fileRootPath = fileRootPath + "/";
        }
        String realPath = fileRootPath + item.getPath();
        doWriteFile(realPath);
        FileInfo temp = new FileInfo();
        temp.setId(item.getId());
        temp.setHlsStatus(YesOrNoEnum.YES.getValue());
        fileInfoService.up(temp);

    }

    //
//    public static void main(String[] args) {
//        VedioWorker work=new VedioWorker();
//        work.fileRootPath="D:/home/upload/fileroot" ;
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setPath("video/52a25d7f77f64140b5c43104a586b6f4.mp4");
//        work.appDomain="img.60community.com";
//        work.doExecuteIndexFile(fileInfo);
//
//
//    }
    public void doWriteFile(String realPath) {
        int indexFile = realPath.lastIndexOf(".");
        int startFile = realPath.lastIndexOf("/");
        String name = realPath.substring(startFile, indexFile);
        realPath = realPath.substring(0, indexFile) + "/default.m3u8";
        // 读
        File file = new File(realPath);
        FileReader in = null;
        FileWriter out = null;
        BufferedReader bufIn = null;
        CharArrayWriter tempStream = null;
        try {
            in = new FileReader(file);
            bufIn = new BufferedReader(in);
            // 内存流, 作为临时流
            tempStream = new CharArrayWriter();
            // 替换
            String line = null;
            while ((line = bufIn.readLine()) != null) {
                // 替换每行中, 符合条件的字符串
                if (line.endsWith(".ts")) {
                    line = "http://" + appDomain + "/video" + name + "/" + line;
                }
                tempStream.write(line);
                line = null;
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));
            }
            // 将内存中的流 写入 文件
            out = new FileWriter(file);
            tempStream.writeTo(out);
        } catch (Exception e) {
            logger.error("写文视频索引件失败", e);
        } finally {
            IOUtils.closeQuietly(tempStream);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufIn);
            IOUtils.closeQuietly(out);
        }
    }

    public void doVideoFile(final FileInfo item) {
        Runnable run = () -> {
            if (!fileRootPath.endsWith("/")) {
                fileRootPath = fileRootPath + "/";
            }
            String realPath = fileRootPath + item.getPath();
            int index = realPath.lastIndexOf("/");
            String path = realPath.substring(0, index);
            String file = realPath.substring(index);
            boolean result = false;
            try {
                result = FFMPegUtils.split(path, file);
            } catch (Exception e) {
                result = false;
                logger.error("执行文件转换失败!", e);
            }
            if (result) {
                FileInfo temp = new FileInfo();
                temp.setId(item.getId());
                temp.setStatus(YesOrNoEnum.YES.getValue());
                temp.setHlsStatus(YesOrNoEnum.NO.getValue());
                fileInfoService.up(temp);
            } else {
                FileInfo temp = new FileInfo();
                temp.setStatus(YesOrNoEnum.YES.getValue());
                if (item.getExecCount() == null) {
                    temp.setExecCount(1);
                } else {
                    temp.setExecCount(item.getExecCount() + 1);
                }
                fileInfoService.up(temp);
            }
        };
        fixedThreadPool.execute(run);
    }

}
