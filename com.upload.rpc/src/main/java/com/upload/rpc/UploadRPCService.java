package com.upload.rpc;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.data.domain.Pageable;

import java.util.List;

@FeignClient("upload.rpc")
public interface UploadRPCService {
    /***
     * 获取文件信息
     * @param fileId
     * @return
     */
    FileInfo findById(String fileId);

    /**
     * 查询文件列表
     * @param query
     * @param pageable
     * @return
     */
    List<FileInfo> query(FileInfo query, Pageable pageable);

    /**
     * 查询视频信息
     * @param fileId
     * @return
     */
    List<FileInfo> queryVideosByFileId(String fileId);
}
