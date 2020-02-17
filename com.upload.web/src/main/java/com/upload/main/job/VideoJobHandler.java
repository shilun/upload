package com.upload.main.job;

import com.upload.service.FileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class VideoJobHandler  {

    @Resource
    private FileInfoService fileInfoService;

    private volatile AtomicBoolean open=new AtomicBoolean(false);

    /**
     * 每30秒执行文件分割任务
     */
    @Scheduled(fixedRate = 30000)
    public void openPeriodOne(){
        if (!open.getAndSet(true)) {
            fileInfoService.doVedioSplit();
            open.set(false);
        }
    }
}
