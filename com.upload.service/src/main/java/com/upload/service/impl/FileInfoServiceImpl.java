package com.upload.service.impl;

import com.common.exception.BizException;
import com.common.redis.RedisDbDao;
import com.common.util.AbstractBaseDao;
import com.common.util.DefaultBaseService;
import com.common.util.GlosseryEnumUtils;
import com.common.util.StringUtils;
import com.common.util.model.YesOrNoEnum;
import com.upload.dao.FileInfoDao;
import com.upload.domain.FileInfo;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.FileUploadConfigService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileInfoServiceImpl extends DefaultBaseService<FileInfo> implements FileInfoService {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(FileInfoServiceImpl.class);
    public static final String UPLOAD_ERROR_BIZ_KEY_EMPTY = "001";
    public static final String UPLOAD_ERROR_BIZ_KEY_ERROR = "002";
    public static final String UPLOAD_ERROR_BIZ_FILE_EMPTY = "003";
    public static final String UPLOAD_ERROR_BIZ_TYPE_ERROR = "004";
    public static final String UPLOAD_ERROR = "005";
    public static final String DOWNLOAD_ERROR = "006";
    public static final String DOWNLOAD_FILE_NOT_FOUND = "007";
    @Resource
    private FileInfoDao fileFileInfoDao;
    @Resource
    private FileUploadConfigService fileUploadConfigService;
    private List<String> pics = new ArrayList();
    @Resource(name = "fileRootPath")
    private String fileRootPath;
    @Resource
    private RedisDbDao redisDbDao;
    @Resource
    private ImageProcessor imageProcessor;

    public FileInfoServiceImpl() {
        this.pics.add("jpg");
        this.pics.add("png");
        this.pics.add("gif");
        this.pics.add("jpeg");
    }

    public AbstractBaseDao<FileInfo> getBaseDao() {
        return this.fileFileInfoDao;
    }

    public Long add(FileInfo entity) {
        throw new RuntimeException("调用添加失败");
    }

    public Map<String, Object> downFile(String key, String fileName) {
        String keyString = MessageFormat.format("UPLOAD.FILE.CONFIG.KEY.{0}", new Object[]{key});
        Map<String, Object> result = new HashMap();
        FileUploadConfig config = (FileUploadConfig) this.redisDbDao.getBySerialize(keyString);
        if (config == null) {
            FileUploadConfig query = new FileUploadConfig();
            query.setCode(key);
            query.setDelStatus(YesOrNoEnum.NO.getValue());
            config = (FileUploadConfig) this.fileUploadConfigService.findByOne(query);
            this.redisDbDao.setexBySerialize(keyString, 1800, config);
        }
        if (config == null) {
            throw new BizException("002", "业务码标识失败");
        }
        FileInfo fileQuery = new FileInfo();
        fileQuery.setPath(config.getScode() + "/" + fileName);
        keyString = MessageFormat.format("UPLOAD.FILE.PATH.KEY.{0}", new Object[]{config.getScode() + "/" + fileName});
        FileInfo findByOne = (FileInfo) this.redisDbDao.getBySerialize(keyString);
        if (findByOne == null) {
            findByOne = (FileInfo) this.fileFileInfoDao.findByOne(fileQuery);
            this.redisDbDao.setexBySerialize(keyString, 1800, findByOne);
        }
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
        String keyString = MessageFormat.format("UPLOAD.FILE.CONFIG.KEY.{0}", new Object[]{scode});
        Map<String, Object> result = new HashMap();
        FileUploadConfig config = (FileUploadConfig) this.redisDbDao.getBySerialize(keyString);
        if (config == null) {
            FileUploadConfig query = new FileUploadConfig();
            query.setScode(scode);
            config = fileUploadConfigService.findByOne(query);
            redisDbDao.setBySerialize(keyString, config);
        }
        if (config.getFileType() == FileTypeEnum.PICTURE.getValue().intValue()) {
            String prefix = file.substring(file.lastIndexOf("."));
            String fileName = file.substring(0, file.lastIndexOf("."));
            if (StringUtils.isNotBlank(size)) {
                size = "_" + size;
            }
            file = fileName + File.separator + fileName + size + prefix;
        }
        String filePath = this.fileRootPath;
        if ((!filePath.endsWith("/")) && (!filePath.endsWith("\\"))) {
            filePath = filePath + "/" + scode + "/" + file;
        }

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


    public String upload(MultipartFile file, String key) {
        String keyString = MessageFormat.format("UPLOAD.FILE.CONFIG.KEY.{0}", new Object[]{key});
        if (StringUtils.isBlank(key)) {
            throw new BizException("001", "业务码不能为空");
        }
        FileUploadConfig config = (FileUploadConfig) this.redisDbDao.getBySerialize(keyString);
        if (config == null) {
            FileUploadConfig query = new FileUploadConfig();
            query.setCode(key);
            query.setDelStatus(YesOrNoEnum.NO.getValue());
            config = (FileUploadConfig) this.fileUploadConfigService.findByOne(query);
            this.redisDbDao.setexBySerialize(keyString, 1800, config);
        }
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
        if (FileTypeEnum.PICTURE.getValue().intValue() == config.getFileType()) {
            if (!this.pics.contains(extFile)) {
                throw new BizException("004", "图片类型不符合规范");
            }
            String uuid = StringUtils.getUUID();
            File picDir = new File(pathFile + File.separator + uuid);
            picDir.mkdirs();
            fileRealName = uuid + "." + extFile;
            targetFile = new File(picDir, fileRealName);
            try {
                file.transferTo(targetFile);
            } catch (Exception e) {
                throw new BizException("005", "文件上传失败，请重试");
            }
            imageProcessor.saveImage(targetFile, null);
        } else {
            String uuid = StringUtils.getUUID();
            File picDir =pathFile;
            picDir.mkdirs();
            fileRealName = uuid + "." + extFile;
            targetFile = new File(picDir, fileRealName);
            try {
                file.transferTo(targetFile);
            } catch (Exception e) {
                throw new BizException("005", "文件上传失败，请重试");
            }
        }
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getOriginalFilename());
        String path = config.getScode() + "/" + fileRealName;
        fileInfo.setPath(path);
        fileInfo.setSize(Integer.valueOf((int) targetFile.length() / 1024));
        this.fileFileInfoDao.add(fileInfo);
        return fileRealName;
    }

    public List<String> getPictures() {
        return this.pics;
    }
}
