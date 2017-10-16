package com.upload.service.worker;

import com.common.util.model.YesOrNoEnum;
import com.upload.domain.FileInfo;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.utils.FFMPegUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private volatile AtomicBoolean runing = new AtomicBoolean(false);

    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60+1)
    public void execute() {
        if (!runing.getAndSet(true)) {
            FileInfo query = new FileInfo();
            query.setType(FileTypeEnum.VEDIO.getValue());
            query.setStatus(YesOrNoEnum.NO.getValue());
            query.setDelStatus(YesOrNoEnum.NO.getValue());
            query.setMaxExecCount(3);
            List<FileInfo> query1 = fileInfoService.query(query);
            for (FileInfo item : query1) {
                doVideoFile(item);
            }
            runing.set(false);
        }
    }

    public void doVideoFile(final FileInfo item) {
        Runnable run = () -> {
            if(!fileRootPath.endsWith("/")){
                fileRootPath=fileRootPath+"/";
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
