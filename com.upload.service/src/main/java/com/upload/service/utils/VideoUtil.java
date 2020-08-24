package com.upload.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadVideoRequest;
import com.aliyun.vod.upload.resp.UploadVideoResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.*;
import com.common.exception.ApplicationException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class VideoUtil {


    @Value("${app.ali.accessKey.id}")
    private String accessKeyId;
    @Value("${app.ali.accessKey.secret}")
    private String accessKeySecret;

    @Value("${app.ali.video.call.back}")
    private String videoCallBack;

    private DefaultAcsClient client;

    /**
     * 初始化
     *
     * @return
     * @throws
     */

    @PostConstruct
    public void initVodClient() throws ClientException {
        String regionId = "cn-shanghai";  // 点播服务接入区域
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        this.client = new DefaultAcsClient(profile);
    }

    /**
     * 本地文件上传接口
     *
     * @param title
     * @param fileName
     */
    public String uploadVideo(String title, String fileName) {
        try {
            UploadVideoRequest request = new UploadVideoRequest(accessKeyId, accessKeySecret, title, fileName);
            /* 可指定分片上传时每个分片的大小，默认为1M字节 */
            request.setPartSize(1 * 1024 * 1024L);
            /* 可指定分片上传时的并发线程数，默认为1，(注：该配置会占用服务器CPU资源，需根据服务器情况指定）*/
            request.setTaskNum(1);
            JSONObject userData = new JSONObject();

            request.setEnableCheckpoint(false);
            JSONObject messageCallback = new JSONObject();
            messageCallback.put("CallbackURL", videoCallBack);
            messageCallback.put("CallbackType", "http");
            userData.put("MessageCallback", messageCallback.toString());
            request.setUserData(userData.toJSONString());

            UploadVideoImpl uploader = new UploadVideoImpl();
            UploadVideoResponse response = uploader.uploadVideo(request);
            if (response.isSuccess()) {
                return response.getVideoId();
            }
            throw new ApplicationException("uploadVideo.error");
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException("uploadVideo.error", e);
        }
    }


    @SneakyThrows
    public static void main(String[] args) {
        VideoUtil util = new VideoUtil();
        util.accessKeyId = "LTAI4GJijReQZWiB2tBYMHSm";
        util.accessKeySecret = "XCoZGf8q4GgDE6JUvXQ1KUyiBIOREV";
        util.initVodClient();
        String videoId = util.uploadVideo("fdsafads", "/Users/mac/Documents/2ebae7a17dbb40eca7aaedc0df906857.mp4");

        SubmitTranscodeJobsResponse submitTranscodeJobsResponse = util.submitTranscodeJobs(videoId);
    }

    /**
     * 提交媒体处理作业
     */
    public SubmitTranscodeJobsResponse submitTranscodeJobs(String videoId) {
        try {
            SubmitTranscodeJobsRequest request = new SubmitTranscodeJobsRequest();
            //需要转码的视频ID
            request.setVideoId(videoId);
            //转码模板ID
            request.setTemplateGroupId("2e8a222b8cac3cb467650271bb58c2de");
            JSONObject userData = new JSONObject();
            JSONObject messageCallback = new JSONObject();
            messageCallback.put("CallbackURL", "http://upload.inteeer.com/transCallBack");
            messageCallback.put("CallbackType", "http");
            userData.put("MessageCallback", messageCallback.toString());

            SubmitTranscodeJobsResponse acsResponse = client.getAcsResponse(request);
            return acsResponse;
        } catch (Exception e) {
            throw new ApplicationException("transcode.error", e);
        }
    }


    /**
     * 获取视频信息
     *
     * @param ids
     * @return
     * @throws Exception
     */
    public GetVideoInfosResponse getVideoInfos(String ids) {
        try {
            GetVideoInfosRequest request = new GetVideoInfosRequest();
            request.setVideoIds(ids.toString());
            return client.getAcsResponse(request);
        } catch (Exception e) {
            throw new ApplicationException("transcode.error", e);
        }
    }

    /**
     * 获取播放地址
     *
     * @param videoId
     * @return
     * @throws Exception
     */
    public GetPlayInfoResponse getPlayInfo(String videoId) {
        try {
            GetPlayInfoRequest request = new GetPlayInfoRequest();
            request.setVideoId(videoId);
            return client.getAcsResponse(request);
        } catch (Exception e) {
            throw new ApplicationException("getPlayInfo.error", e);
        }
    }

}
