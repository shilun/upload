package com.upload.main.job;


import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.common.util.model.OrderTypeEnum;
import com.common.util.model.YesOrNoEnum;
import com.upload.domain.FileInfo;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.process.AtUtils;
import com.upload.service.utils.VideoUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@JobHandler(value = "VideoJobHandler")
@Slf4j
@Component
public class VideoJobHandler extends IJobHandler {

    @Resource
    private AtUtils atUtils;

    @Resource
    private VideoUtil videoUtil;

    @Resource
    private FileInfoService fileInfoService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            FileInfo query = new FileInfo();
            query.setType(FileTypeEnum.VIDEO.getValue());
            query.setHlsStatus(YesOrNoEnum.NO.getValue());
            query.setOrderType(OrderTypeEnum.ASC);
            Page<FileInfo> fileInfos = fileInfoService.queryByPage(query, PageRequest.of(0, 10));
            if (fileInfos.getContent().size() > 1) {
                for (FileInfo info : fileInfos.getContent()) {
                    boolean playUrlOk=false;
                    GetPlayInfoResponse playInfo = videoUtil.getPlayInfo(info.getId());
                    List<GetPlayInfoResponse.PlayInfo> playes = playInfo.getPlayInfoList();
                    for (GetPlayInfoResponse.PlayInfo item : playes) {
                        if (item.getPlayURL().endsWith("m3u8")) {
                            info.setVideoUrl(item.getPlayURL());
                            playUrlOk=true;
                        }
                        if (item.getPlayURL().endsWith("mp4")) {
                            info.setVideoSource(item.getPlayURL());
                        }
                    }
                    if(playUrlOk){
                        info.setVideoImage(videoUtil.getVideoInfos(info.getId()).getVideoList().get(0).getCoverURL());
                        info.setHlsStatus(YesOrNoEnum.YES.getValue());
                        fileInfoService.save(info);
                    }
                }
            }
        } catch (Exception e) {
            log.error("执行视频文件分片重试失败", e);
        }
        return SUCCESS;
    }

}
