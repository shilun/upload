package com.upload.service.impl;

import com.common.exception.BizException;
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
import com.upload.service.utils.FFMPegUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class FileInfoServiceImpl extends AbstractMongoService<FileInfo> implements FileInfoService {
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
    private List<String> pics = new ArrayList();
    private List<String> videos = new ArrayList<>();
    @Value("${app.fileRootPath}")
    private String fileRootPath;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ImageProcessor imageProcessor;

    @Resource(name = "asyncWorkerExecutor")
    private Executor executor;

    public FileInfoServiceImpl() {
        this.pics.add("jpg");
        this.pics.add("png");
        this.pics.add("gif");
        this.pics.add("jpeg");
        videos.add("mp4");
    }

    private com.google.common.cache.Cache<String, FileUploadConfig> scodeCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(1, TimeUnit.HOURS).build();

    @Override
    public void doVedioSplit() {
        Criteria criteria = Criteria.where("type").is(FileTypeEnum.VIDEO.getValue()).and("status").is(YesOrNoEnum.NO.getValue()).and("hlsStatus").is(YesOrNoEnum.NO.getValue());
        Query query = new Query(criteria);
        Page<FileInfo> fileInfos = queryByPage(query, PageRequest.of(0, 100));
        for (FileInfo item : fileInfos.getContent()) {
            doVideoFile(item);
        }
    }

    public Map<String, Object> downFile(String key, String fileName) {
        Map<String, Object> result = new HashMap();
        FileUploadConfig config = findConfigByKey(key);
        if (config == null) {
            throw new BizException("002", "业务码标识失败");
        }
        FileInfo fileQuery = new FileInfo();
        fileQuery.setPath(config.getScode() + "/" + fileName);
        FileInfo findByOne = this.findByOne(fileQuery);
        if (findByOne == null) {
            logger.error("文件记录未找到 key:" + key + " path:" + config.getScode() + "/" + fileName);
            throw new BizException("006", "文件下载失败");
        }
        FileInputStream input = null;
        try {
            result.put("fileName", findByOne.getName());
            result.put("fileType", GlosseryEnumUtils.getItem(FileTypeEnum.class, config.getFileType()));
            input = new FileInputStream(this.fileRootPath + "/" + findByOne.getPath());
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
        Map<String, Object> result = new HashMap();

        FileUploadConfig config = findConfigByScode(scode);
        String fileRootPath = null;
        String rootPath = null;
        if (!this.fileRootPath.endsWith("/")) {
            rootPath = this.fileRootPath + "/";
        } else {
            rootPath = this.fileRootPath;
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
                String sourceFile = rootPath + scode + "/" + fileName + File.separator + fileName + prefix;
                String[] sizeStr = size.substring(1).split("x");
                imageProcessor.saveImage(new File(sourceFile), Integer.parseInt(sizeStr[0]), Integer.parseInt(sizeStr[1]));
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
        }
        else{
            filePath = filePath  + scode + "/" + file;
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

    private FileUploadConfig findConfigByKey(String key) {
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

    protected int vedioSize = 1048576;

    public String upload(MultipartFile file, String key) {
        if (StringUtils.isBlank(key)) {
            throw new BizException("001", "业务码不能为空");
        }

        FileUploadConfig config = findConfigByKey(key);
        if (config == null) {
            throw new BizException("002", "业务码标识失败");
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
        boolean other = true;
        String uuid = StringUtils.getUUID();
        if (FileTypeEnum.PICTURE.getValue().intValue() == config.getFileType()) {
            if (!this.pics.contains(extFile)) {
                throw new BizException("004", "图片类型不符合规范");
            }

            File picDir = new File(pathFile + File.separator + uuid);
            picDir.mkdirs();
            fileRealName = uuid + "." + extFile;
            targetFile = new File(picDir, fileRealName);
            try {
                file.transferTo(targetFile.getAbsoluteFile());
            } catch (Exception e) {
                throw new BizException("005", "文件上传失败，请重试");
            }
            imageProcessor.saveImage(targetFile, null);
            other = false;
        } else {
            File picDir = pathFile;
            picDir.mkdirs();
            fileRealName = uuid + "." + extFile;
            targetFile = new File(picDir, fileRealName);
            try {
                file.transferTo(targetFile.getAbsoluteFile());
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new BizException("005", "文件上传失败，请重试");
            }
        }
        FileInfo fileInfo = new FileInfo();
        if (config.getFileType().intValue() == FileTypeEnum.VIDEO.getValue()) {
            fileRealName = uuid + "/default.m3u8";
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
        this.insert(fileInfo);
        return fileRealName;
    }

    public List<String> getPictures() {
        return this.pics;
    }

    private void doVideoFile(final FileInfo item) {
        Runnable run = () -> {
            if (!fileRootPath.endsWith("/")) {
                fileRootPath = fileRootPath + "/";
            }
            String realPath = fileRootPath + item.getPath();
            realPath = realPath.replace("/default.m3u8", ".mp4");
            int index = realPath.lastIndexOf("/");
            String path = realPath.substring(0, index);
            String file = realPath.substring(index);
            FFMPegUtils.doExportImage(path, file);
            FFMPegUtils.split(path, file);
            FileInfo temp = new FileInfo();
            temp.setId(item.getId());
            temp.setStatus(YesOrNoEnum.YES.getValue());
            temp.setHlsStatus(YesOrNoEnum.NO.getValue());
            save(temp);
        };
        executor.execute(run);
    }
}
