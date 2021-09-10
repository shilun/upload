package com.upload.main.controller;

import com.common.exception.BizException;
import com.common.upload.UploadUtil;
import com.common.util.Result;
import com.common.util.StringUtils;
import com.common.web.AbstractController;
import com.common.web.IExecute;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.main.util.FileType;
import com.upload.service.FileInfoService;
import com.upload.service.FileUploadConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping
@Slf4j
public class FileInfoController extends AbstractController {

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private FileUploadConfigService fileUploadConfigService;
    private static List<String> images = new ArrayList<>();

    static {
        images.add("jpg");
        images.add("png");
        images.add("gif");
        images.add("jpeg");
    }


    @RequestMapping("/download")
    public ResponseEntity donwload(DownloadDto dto, HttpServletResponse response) throws Exception {
        Map<String, Object> downFile = this.fileInfoService.downFile(dto.getKey(), dto.getFileName());
        String realName = (String) downFile.get("fileName");
        String filePath = (String) downFile.get("data");
        return download(new File(filePath), realName);
    }

    @Value("${app.fileRootPath}")
    private String fileRootPath;


    @RequestMapping("{size}/{scode}/{file}.{fileType}")
    public ResponseEntity downloadImageSize(@PathVariable String size, @PathVariable String scode, @PathVariable String file, @PathVariable String fileType, HttpServletResponse response) throws Exception {
        FileUploadConfig config = fileInfoService.findConfigByScode(scode);
        if (!config.getHttpDown()) {
            Map result = new HashMap();
            result.put("success", false);
            result.put("message", "当前下载桶不充许使用http下载");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }
        File realFile = this.fileInfoService.httpDown(scode, file + "." + fileType, size);
        if (images.contains(fileType)) {
            return download(realFile, realFile.getName(), MediaType.IMAGE_JPEG);
        } else {
            file = file + "." + fileType;
        }
        return download(new File(file), realFile.getName());
    }


    @RequestMapping("{scode}/{file}.{fileType}")
    public ResponseEntity downloadResource(@PathVariable("scode") String scode, @PathVariable("file") String file, @PathVariable("fileType") String fileType, HttpServletResponse response) {
        try {
            FileUploadConfig config = fileInfoService.findConfigByScode(scode);
            if (!config.getHttpDown()) {
                Map result = new HashMap();
                result.put("success", false);
                result.put("message", "当前下载桶不充许使用http下载");
                return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
            }
            file = file + "." + fileType;
            File currentFile = fileInfoService.httpDown(scode, file);
            if (config.getFileType() == FileTypeEnum.PICTURE) {
                return download(currentFile, currentFile.getName(), MediaType.IMAGE_JPEG);
            }
            String alias = request.getParameter("alias");
            if (StringUtils.isBlank(alias)) {
                alias = currentFile.getName();
            }
            return download(currentFile, alias);

        } catch (Exception e) {
            log.error("download.{}.{}. message:{}", scode, file + "." + fileType, e.getMessage());
            Map result = new HashMap();
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }
    }

    private ResponseEntity download(File file, String realName, MediaType mediaType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (mediaType == MediaType.IMAGE_JPEG) {
            headers.setContentType(MediaType.IMAGE_JPEG);
            return ResponseEntity.ok().headers(headers).body(FileCopyUtils.copyToByteArray(file));
        }
        headers.add("Content-Disposition", "attachment; filename=" + realName);
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(mediaType).body(new FileSystemResource(file));
    }

    private ResponseEntity download(File file, String realName) throws Exception {
        return download(file, realName, MediaType.APPLICATION_OCTET_STREAM);
    }

    @RequestMapping("/upload")
    @ResponseBody
    public Map<String, Object> upload(final MultipartFile file, final String key) {
        return buildMessage(new IExecute() {
            public Object getData() {
                FileUploadConfig config = fileInfoService.findConfigByKey(key);
                if (config == null) {
                    throw new BizException("002", "业务码标识失败");
                }
                return fileInfoService.upload(file, key, config);
            }
        });
    }

    public static void main(String[] args) throws IOException {

        byte[] bytes1 = IOUtils.toByteArray(new FileInputStream(new File("d:/tt.png")));
        byte[] typeData = Arrays.copyOf(bytes1, 10);
        String fileType = FileType.getFileType(typeData);

        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("http://127.0.0.1:8081");
        uploadUtil.setScode("img");
        uploadUtil.setCode("dc7220d477a249fcbcb6688d62a5e73e");

//        uploadUtil.setScode("img");
//        uploadUtil.setCode("88c0c97d2983479597130e1c96a25115");
        for (int i = 0; i < 100; i++) {
            Result<String> stringResult = uploadUtil.uploadFile(new File("d:/tt.png"));
            byte[] bytes = uploadUtil.downFile(stringResult.getModule());
            IOUtils.write(bytes, new FileOutputStream(new File("d:/new1ss.jpg")));
        }
//        IOUtils.write(bytes, new FileOutputStream(new File("/Users/mac/new1ss.jpg")));
//        System.out.println(bytes);
    }
}
