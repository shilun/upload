package com.upload.service.impl;

import com.common.exception.BizException;
import com.common.httpclient.HttpClientUtil;
import com.common.mongo.AbstractMongoService;
import com.common.util.StringUtils;
import com.google.common.cache.CacheBuilder;
import com.upload.domain.FileInfo;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.FileUploadConfigService;
import com.upload.service.process.AtUtils;
import com.upload.util.constants.SystemConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
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

@Slf4j
@Service
public class FileInfoServiceImpl extends AbstractMongoService<FileInfo> implements FileInfoService {
    private HttpClientUtil httpClientUtil = new HttpClientUtil();
    private static final long serialVersionUID = 1L;
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
    private Executor executor;

    public FileInfoServiceImpl() {

    }


    private com.google.common.cache.Cache<String, FileUploadConfig> scodeCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(1, TimeUnit.HOURS).build();


    public Map<String, Object> downFile(String key, String fileName) {
        Map<String, Object> result = new HashMap();
        FileUploadConfig config = findConfigByKey(key);
        if (config == null) {
            throw new BizException("002", "业务码标识失败");
        }
        String realName = this.fileRootPath + "/" + config.getScode() + "/" + fileName;
        File downFile = new File(realName);
        if (!downFile.exists()) {
            log.error("文件记录未找到 key:" + key + " path:" + config.getScode() + "/" + fileName);
            throw new BizException("006", "文件下载失败");
        }
        result.put("fileType", config.getFileType());
        result.put("fileName", fileName);
        result.put("data", realName);

        return result;
    }

    public File httpDown(String scode, String file, String size) {
        String fileRootPath = null;
        String rootPath = null;
        if (!this.fileRootPath.endsWith("/")) {
            rootPath = this.fileRootPath + "/";
        } else {
            rootPath = this.fileRootPath;
        }
        if (StringUtils.isNotBlank(size)) {
            String prefix = file.substring(file.lastIndexOf("."));
            String fileName = file.substring(0, file.lastIndexOf("."));
            if (StringUtils.isNotBlank(size)) {
                size = "_" + size;
            }
            file = fileName + File.separator + fileName + size + prefix;
            fileRootPath = rootPath + scode + "/" + file;
            return new File(fileRootPath);
        }
        String filePath = rootPath + scode + "/" + file;
        return new File(fileRootPath);
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
            log.error("FileInfoServiceImpl.downFile.error", e);
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
            log.error("FileInfoServiceImpl.downFile.error", e);
        }
        return null;
    }


    @SneakyThrows
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
            file = null;
        } catch (Exception e) {
            log.error("file.upload.error", e);
            throw new BizException("005", "文件上传失败，请重试");
        }

        FileInfo fileInfo = new FileInfo();

        fileInfo.setStatus(true);
        fileInfo.setExecCount(0);
        fileInfo.setName(fileName);
        String path = config.getScode() + "/" + fileRealName;
        fileInfo.setPath(path);
        fileInfo.setSize(Integer.valueOf((int) targetFile.length() / 1024));
        fileInfo.setId(uuid);
        this.insert(fileInfo);
        if (FileTypeEnum.PICTURE==config.getFileType()) {
            BufferedImage sourceImg = ImageIO.read(new FileInputStream(targetFile.getAbsoluteFile()));
            String[] itemSize = systemConstants.getDefaultImageSize().split(",");
            for (String item : itemSize) {
                String[] xes = item.split("x");
                int height=Integer.parseInt(xes[0]);
                String target = pathFile + File.separator + uuid + File.separator + uuid + "_" + item + "." + extFile;
                Boolean soft=true;
                if(sourceImg.getHeight()>height||sourceImg.getWidth()>height){
                    soft=false;
                }
                imageProcessor.resize(targetFile, height, height, target,soft);
            }
        }
        return fileRealName;
    }
}
