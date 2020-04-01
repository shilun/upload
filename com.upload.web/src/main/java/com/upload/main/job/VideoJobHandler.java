package com.upload.main.job;

import com.upload.service.FileInfoService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@JobHandler(value = "VideoJobHandler")
@Slf4j
@Component
public class VideoJobHandler extends IJobHandler {

    @Resource
    private FileInfoService fileInfoService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        fileInfoService.doVedioSplit();
        return SUCCESS;
    }

}
