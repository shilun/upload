package com.upload.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.SubmitTranscodeJobsRequest;
import com.aliyuncs.vod.model.v20170321.SubmitTranscodeJobsResponse;
import com.common.exception.ApplicationException;
import com.common.security.DesEncrypter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class VideoUtil {


    @Value("${app.ali.accessKey.id}")
    private String accessKeyId;
    @Value("${app.ali.accessKey.secret}")
    private String accessKeySecret;

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
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setTitle(title);
            request.setFileName(fileName);

            JSONObject userData = new JSONObject();

            JSONObject messageCallback = new JSONObject();
            messageCallback.put("CallbackURL", "http://upload.inteeer.com/callBack");
            messageCallback.put("CallbackType", "http");
            userData.put("MessageCallback", messageCallback.toString());

            JSONObject extend = new JSONObject();
            extend.put("time", System.currentTimeMillis());
            extend.put("enkey", DesEncrypter.cryptString(extend.getString("time"), "bsm_key_key"));
            userData.put("Extend", extend.toJSONString());
            request.setUserData(userData.toJSONString());

            return client.getAcsResponse(request).getVideoId();
        } catch (Exception e) {
            throw new ApplicationException("uploadVideo.error", e);
        }
    }



    /**
     * 提交媒体处理作业
     */
    public static SubmitTranscodeJobsResponse submitTranscodeJobs(DefaultAcsClient client) throws Exception {
        SubmitTranscodeJobsRequest request = new SubmitTranscodeJobsRequest();
        //需要转码的视频ID
        request.setVideoId("b64d29ac2f174f09952ba441db0a5fc1");
        //转码模板ID
        request.setTemplateGroupId("892483e93e26f646dd876138548f3a22");

        JSONObject userData = new JSONObject();

        JSONObject messageCallback = new JSONObject();
        messageCallback.put("CallbackURL", "http://upload.inteeer.com/transTemplateCallBack");
        messageCallback.put("CallbackType", "http");
        userData.put("MessageCallback", messageCallback.toString());

        return client.getAcsResponse(request);
    }




}
