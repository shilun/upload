package com.upload.service.impl;

import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.common.exception.BizException;
import com.common.httpclient.HttpClientUtil;
import com.common.mongo.AbstractMongoService;
import com.common.util.GlosseryEnumUtils;
import com.common.util.StringUtils;
import com.common.util.model.YesOrNoEnum;
import com.google.common.cache.CacheBuilder;
import com.upload.domain.FileInfo;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.FileUploadConfigService;
import com.upload.service.process.AtUtils;
import com.upload.service.utils.VideoUtil;
import com.upload.util.constants.SystemConstants;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class FileInfoServiceImpl extends AbstractMongoService<FileInfo> implements FileInfoService {
    private HttpClientUtil httpClientUtil = new HttpClientUtil();
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);
    public static final String UPLOAD_ERROR_BIZ_KEY_EMPTY = "001";
    public static final String UPLOAD_ERROR_BIZ_KEY_ERROR = "002";
    public static final String UPLOAD_ERROR_BIZ_FILE_EMPTY = "003";
    public static final String UPLOAD_ERROR_BIZ_TYPE_ERROR = "004";
    public static final String UPLOAD_ERROR = "005";
    public static final String DOWNLOAD_ERROR = "006";
    public static final String DOWNLOAD_FILE_NOT_FOUND = "007";


    @Override
    protected Class getEntityClass() {
        return FileInfo.class;
    }

    @Resource
    private FileUploadConfigService fileUploadConfigService;
    private List<String> videos = new ArrayList<>();
    @Value("${app.fileRootPath}")
    private String fileRootPath;
    @Resource
    private SystemConstants systemConstants;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ImageProcessor imageProcessor;


    @Resource
    private AtUtils atUtils;
    @Resource
    private VideoUtil videoUtil;
    @Resource
    private Executor executor;

    public FileInfoServiceImpl() {

    }


    private com.google.common.cache.Cache<String, FileUploadConfig> scodeCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(1, TimeUnit.HOURS).build();


    @Override
    public FileInfo findVideoByVideoId(String voideId) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setVideoId(voideId);
        return findByOne(fileInfo);
    }


    @Override
    public FileInfo syncVideoInfo(FileInfo video) {
        FileInfo info = new FileInfo();
        info.setId(video.getId());
        boolean playUrlOk = false;
        int indexCounter = 0;
        while (indexCounter < 20) {
            GetPlayInfoResponse playInfo = videoUtil.getPlayInfo(video.getVideoId());
            List<GetPlayInfoResponse.PlayInfo> playes = playInfo.getPlayInfoList();
            for (GetPlayInfoResponse.PlayInfo item : playes) {
                if (item.getPlayURL().endsWith("m3u8")) {
                    info.setVideoUrl(item.getPlayURL());
                    video.setVideoUrl(info.getVideoUrl());
                    playUrlOk = true;
                }
                if (item.getPlayURL().endsWith("mp4")) {
                    info.setVideoSource(item.getPlayURL());
                    video.setVideoSource(item.getPlayURL());
                }
            }
            if (playUrlOk) {
                String imageUrl = playInfo.getVideoBase().getCoverURL();
                if (StringUtils.isBlank(imageUrl)) {
                    continue;
                }
                info.setVideoImage(playInfo.getVideoBase().getCoverURL());
                info.setHlsStatus(YesOrNoEnum.YES.getValue());
                save(info);
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("获取视频自旋失败", e);
            }
        }
        return info;
    }

    public Map<String, Object> downFile(String key, String fileName) {
        Map<String, Object> result = new HashMap();
        FileUploadConfig config = findConfigByKey(key);
        if (config == null) {
            throw new BizException("002", "业务码标识失败");
        }
        String realName = this.fileRootPath + "/" + config.getScode() + "/" + fileName;
        File downFile = new File(realName);
        if (!downFile.exists()) {
            logger.error("文件记录未找到 key:" + key + " path:" + config.getScode() + "/" + fileName);
            throw new BizException("006", "文件下载失败");
        }
        FileInputStream input = null;
        try {
            result.put("fileName", fileName);
            result.put("fileType", GlosseryEnumUtils.getItem(FileTypeEnum.class, config.getFileType()));
            input = new FileInputStream(downFile);
            byte[] file = IOUtils.toByteArray(input);
            result.put("data", file);
        } catch (FileNotFoundException e) {
            logger.error("文件下载失败，文件不存在 path:" + config.getScode() + "/" + fileName, e);
            throw new BizException("007", "文件下载失败,文件不存在");
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            throw new BizException("006", "文件下载失败");
        } finally {
            IOUtils.closeQuietly(input);
        }
        return result;
    }

    public byte[] httpDown(String scode, String file, String size) {
        String fileRootPath = null;
        String rootPath = null;
        if (!this.fileRootPath.endsWith("/")) {
            rootPath = this.fileRootPath + "/";
        } else {
            rootPath = this.fileRootPath;
        }
        FileUploadConfig config = findConfigByScode(scode);
        if (config.getFileType() == FileTypeEnum.VIDEO.getValue().intValue()) {
            file = file.substring(0, file.indexOf(".")) + "/default.jpeg";
        }
        if (config.getFileType() == FileTypeEnum.PICTURE.getValue().intValue()) {
            String prefix = file.substring(file.lastIndexOf("."));
            String fileName = file.substring(0, file.lastIndexOf("."));
            if (StringUtils.isNotBlank(size)) {
                size = "_" + size;
            }
            file = fileName + File.separator + fileName + size + prefix;
            fileRootPath = rootPath + scode + "/" + file;
            File imageFile = new File(fileRootPath);
            if (!imageFile.exists()) {
                throw new BizException("data.error", "非法操作,尺寸不存在");
//                String sourceFile = rootPath + scode + "/" + fileName + File.separator + fileName + prefix;
//                String[] sizeStr = size.substring(1).split("x");
//                imageProcessor.resize(new File(sourceFile),Integer.parseInt(sizeStr[0]), Integer.parseInt(sizeStr[1]),fileRootPath);
            }

        }
        String filePath = rootPath + scode + "/" + file;

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            return IOUtils.toByteArray(inputStream);
        } catch (FileNotFoundException e) {
            logger.error("文件下载失败，文件不存在 path:" + filePath, e);
            throw new BizException("007", "文件不存在");
        } catch (IOException e) {
            logger.error("文件下载失败", e);
            throw new BizException("006", "文件下载失败");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    public File httpDown(String scode, String file) {
        String filePath = this.fileRootPath;
        if ((!filePath.endsWith("/")) && (!filePath.endsWith("\\"))) {
            filePath = filePath + "/" + scode + "/" + file;
        } else {
            filePath = filePath + scode + "/" + file;
        }
        return new File(filePath);
    }

    public FileUploadConfig findConfigByScode(String scode) {
        try {
            return scodeCache.get(scode, new Callable<FileUploadConfig>() {
                @Override
                public FileUploadConfig call() throws Exception {
                    FileUploadConfig query = new FileUploadConfig();
                    query.setScode(scode);
                    return fileUploadConfigService.findByOne(query);
                }
            });
        } catch (ExecutionException e) {
            logger.error("FileInfoServiceImpl.downFile.error", e);
        }
        return null;
    }

    public FileUploadConfig findConfigByKey(String key) {
        try {
            return scodeCache.get(key, new Callable<FileUploadConfig>() {
                @Override
                public FileUploadConfig call() throws Exception {
                    FileUploadConfig query = new FileUploadConfig();
                    query.setCode(key);
                    return fileUploadConfigService.findByOne(query);
                }
            });
        } catch (ExecutionException e) {
            logger.error("FileInfoServiceImpl.downFile.error", e);
        }
        return null;
    }

    public String upload(MultipartFile file, String key, FileUploadConfig config) {
        if (StringUtils.isBlank(key)) {
            throw new BizException("001", "业务码不能为空");
        }
        File pathFile = new File(this.fileRootPath + File.separator + config.getScode());
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        if ((file == null) || (file.getSize() == 0L)) {
            throw new BizException("003", "文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new BizException("004", "文件名不符合规范");
        }
        String extFile = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        File targetFile = null;
        String fileRealName = null;
        String uuid = StringUtils.getUUID();

        fileRealName = uuid + "." + extFile;
        targetFile = new File(pathFile, fileRealName);
        try {
            file.transferTo(targetFile.getAbsoluteFile());
        } catch (Exception e) {
            throw new BizException("005", "文件上传失败，请重试");
        }

        FileInfo fileInfo = new FileInfo();
        if (config.getFileType().intValue() == FileTypeEnum.VIDEO.getValue()) {
            fileRealName = uuid + ".mp4";
            fileInfo.setStatus(YesOrNoEnum.NO.getValue());
            fileInfo.setType(FileTypeEnum.VIDEO.getValue());
            fileInfo.setHlsStatus(YesOrNoEnum.NO.getValue());
        } else {
            fileInfo.setStatus(YesOrNoEnum.YES.getValue());
        }
        fileInfo.setExecCount(0);
        fileInfo.setName(file.getOriginalFilename());
        String path = config.getScode() + "/" + fileRealName;
        fileInfo.setPath(path);
        fileInfo.setSize(Integer.valueOf((int) targetFile.length() / 1024));
        fileInfo.setId(uuid);
        this.insert(fileInfo);
        if (config.getFileType().intValue() == FileTypeEnum.PICTURE.getValue()) {
            String[] itemSize = systemConstants.getDefaultImageSize().split(",");
            for (String item : itemSize) {
                String[] xes = item.split("x");
                String target = pathFile + File.separator + uuid + File.separator + uuid + "_" + item + "." + extFile;
                imageProcessor.resize(targetFile, Integer.parseInt(xes[0]), Integer.parseInt(xes[1]), target);
            }
        }
        if (config.getFileType().intValue() == FileTypeEnum.VIDEO.getValue()) {
            atUtils.appendFile(pathFile.getPath(),uuid);
        }
        return fileRealName;
    }


}
