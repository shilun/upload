package com.upload.main.controller;

import com.common.exception.BizException;
import com.common.upload.UploadUtil;
import com.common.util.Result;
import com.common.util.model.YesOrNoEnum;
import com.common.web.AbstractController;
import com.common.web.IExecute;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.main.util.FileType;
import com.upload.service.FileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@Slf4j
public class FileInfoController extends AbstractController {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileInfoController.class);
    @Resource
    private FileInfoService fileInfoService;

    private static List<String> images = new ArrayList<>();

    static {
        images.add("jpg");
        images.add("png");
        images.add("gif");
        images.add("jpeg");
    }

    @RequestMapping("/download")
    public void donwload(DownloadDto dto, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> downFile = this.fileInfoService.downFile(dto.getKey(), dto.getFileName());
            FileTypeEnum typeEnum = (FileTypeEnum) downFile.get("fileType");
            String realName = (String) downFile.get("fileName");
            byte[] data = (byte[]) downFile.get("data");
            String typeName = "application/x-download";
            if (typeEnum == FileTypeEnum.PICTURE) {
                typeName = "jpg";
                realName = "";
            }
            download(response, data, typeName, realName);
        } catch (BizException e) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            request.getSession().getServletContext().getRequestDispatcher("/download_erro?code=" + e.getCode() + "&message=" + e.getMessage()).forward(request, response);
        }
    }

    @Value("${app.fileRootPath}")
    private String fileRootPath;


    private void downloadPlayerIndex(String scode, String file, HttpServletResponse response) {
        String path = fileRootPath + "/" + scode + "/" + file + "/default.m3u8";
        File realFile = new File(path);
        try {
            if (realFile.getAbsoluteFile().exists()) {
                byte[] bytes = IOUtils.toByteArray(new FileInputStream(realFile));
                download(response, bytes, "application/vnd.apple.mpegurl", file + ".m3u8");
            }
        } catch (Exception e) {
            LOGGER.error("downloadPlayerIndex.error", e);
        }
    }

    @RequestMapping("/{scode}/{file}/{item}.ts")
    public void downloadVideo(@PathVariable String scode, @PathVariable String file, @PathVariable String item, HttpServletResponse response) throws Exception {
        FileUploadConfig configByScode = fileInfoService.findConfigByScode(scode);
        if (configByScode.getFileType() != FileTypeEnum.VIDEO.getValue().intValue()) {
            throw new BizException("data.error", "非法操作");
        }
        String path = fileRootPath + "/" + configByScode.getScode() + "/" + file + "/" + item + ".ts";
        File realFile = new File(path);
        if (realFile.exists()) {
            downVideo(path, response);
        }
    }

    @RequestMapping("{size}/{scode}/{file}.{fileType}")
    public void downloadImageSize(@PathVariable String size, @PathVariable String scode, @PathVariable String file, @PathVariable String fileType, HttpServletResponse response) {
        if (StringUtils.endsWithIgnoreCase(scode, "video")) {
            return;
        }
        try {
            byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, size);
            String typeName = "application/x-download";
            if (images.contains(fileType)) {
                typeName = "image/" + fileType;
                file = "";
            } else {
                file = file + "." + fileType;
            }
            download(response, data, typeName, file);
        } catch (Exception e) {
            throw new BizException(e);
        }
    }

    @RequestMapping("{size}/{scode}/{file}/{name}.{fileType}")
    public void downloadImageSizeResource(@PathVariable String size, @PathVariable String scode, @PathVariable String file, @PathVariable String name, @PathVariable String fileType, HttpServletResponse response) {
        if (StringUtils.endsWithIgnoreCase(scode, "video")) {
            return;
        }
        try {
            byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, size);
            String typeName = "application/x-download";
            if (images.contains(fileType)) {
                typeName = "image/" + fileType;
                file = "";
            } else {
                file = file + "." + fileType;
            }
            download(response, data, typeName, file);
        } catch (Exception e) {
            throw new BizException(e);
        }
    }


    public void downVideo(String file, HttpServletResponse response) {
        String agent = getRequest().getHeader("User-Agent").toUpperCase();
        File realFile = new File(file);
        InputStream fis = null;
        OutputStream os = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(realFile.getPath()));
            byte[] buffer;
            buffer = new byte[fis.available()];
            fis.read(buffer);
            response.reset();
            //由于火狐和其他浏览器显示名称的方式不相同，需要进行不同的编码处理
            if (agent.indexOf("FIREFOX") != -1) {
                response.addHeader("Content-Disposition", "attachment;filename=vedio.ts");
            } else {//其他浏览器
                response.addHeader("Content-Disposition", "attachment;filename=vedio.ts");
            }
            //设置response编码
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Length", String.valueOf(realFile.length()));
            //设置输出文件类型
            response.setContentType("video/mpeg4");
            //获取response输出流
            os = response.getOutputStream();
            // 输出文件
            os.write(buffer);
        } catch (Exception e) {
            LOGGER.error("输出视频流失败!", e);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(os);
        }
    }


    @RequestMapping("{scode}/{file}.{fileType}")
    public void downloadResource(@PathVariable("scode") String scode, @PathVariable("file") String file, @PathVariable("fileType") String fileType, HttpServletResponse response) {
        try {
            if (fileType.equals("m3u8")) {
                downloadPlayerIndex(scode, file, response);
                return;
            }
            if (scode.startsWith("s")) {
                scode = scode.substring(1);
                byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, "");
                String typeName = "image/" + fileType;
                file = "";
                download(response, data, typeName, file);
                return;
            }
            FileUploadConfig configByScode = fileInfoService.findConfigByScode(scode);
            if (configByScode.getHttpDown().intValue() == YesOrNoEnum.NO.getValue()) {
                throw new BizException("data.error", "非法操作");
            }
            if (images.contains(fileType)) {
                byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, "");
                String typeName = "image/" + fileType;
                file = "";
                download(response, data, typeName, file);
                return;
            } else {
                file = file + "." + fileType;
                File currentFile = fileInfoService.httpDown(scode, file);
                response.setContentType("application/octet-stream");
                if (fileType.startsWith("svg")) {
                    response.setContentType("text/xml");
                }
                if (fileType.startsWith("svg")) {
                    String typeName = "text/xml";
                    byte[] data = FileUtils.readFileToByteArray(currentFile);
                    download(response, data, typeName, file);
                    return;
                }
                byte[] data = FileUtils.readFileToByteArray(currentFile);
                String typeName = "application/octet-stream";
                download(response, data, typeName, file);
            }
        } catch (Exception e) {
            throw new BizException(e);
        }
    }

    @RequestMapping("{scode}/{file}/default.{fileType}")
    public void downloadVideoImage(@PathVariable("scode") String scode, @PathVariable("file") String file, @PathVariable("fileType") String fileType, HttpServletResponse response) {
        try {
            if (fileType.equalsIgnoreCase("m3u8")) {
                downloadPlayerIndex(scode, file, response);
                return;
            }
            if (scode.startsWith("s")) {
                scode = scode.substring(1);
                byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, "");
                String typeName = "image/" + fileType;
                file = "";
                download(response, data, typeName, file);
                return;
            }
        } catch (Exception e) {
            throw new BizException(e);
        }
    }

    @RequestMapping({"/download_erro"})
    @ResponseBody
    public Map<String, Object> downError(final String code, final String message) {
        return buildMessage(new IExecute() {
            public Object getData() {
                throw new BizException(code, message);
            }
        });
    }

    private void download(HttpServletResponse response, byte[] file, String contentType, String realName) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        ByteArrayInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            response.setContentType(contentType);
            if (StringUtils.isNotBlank(realName)) {
                response.setHeader("Content-disposition", "attachment; filename=" + new String(realName.getBytes("utf-8"), "ISO8859-1"));
                response.setHeader("Content-Length", String.valueOf(file.length));
            }
            bis = new ByteArrayInputStream(file);

            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bos.flush();
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(bos);
        }
    }

    @RequestMapping({"/upload"})
    @ResponseBody
    public Map<String, Object> upload(final MultipartFile file, final String key) {

        return buildMessage(new IExecute() {
            public Object getData() {
                FileUploadConfig config = fileInfoService.findConfigByKey(key);
                if (config == null) {
                    throw new BizException("002", "业务码标识失败");
                }
                if (config.getFileType() != 1) {
                    try {
                        byte[] bytes = file.getBytes();
                        byte[] typeData = Arrays.copyOf(bytes, 10);
                        String fileCode = FileType.getFileType(typeData);
                        if (!images.contains(fileCode)) {
                            throw new BizException("data.error", "图片上传失败,请上传jpg/jpeg/png格式图片");
                        }
                        if (!FileType.isMp4(typeData)) {
                            throw new BizException("data.error", "mp4上传失败,请上传mp4视频文件");
                        }
                    } catch (Exception e) {
                        log.error("文件上传失败", e);
                        throw new BizException("data.error", "文件上传失败");
                    }
                }
                return fileInfoService.upload(file, key, config);
            }
        });
    }

    public static void main(String[] args) throws IOException {

        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("upload.inteeer.com");
        uploadUtil.setScode("img");
        uploadUtil.setCode("88c0c97d2983479597130e1c96a25115");
        for (int i = 0; i < 100; i++) {
            Result<String> stringResult = uploadUtil.uploadFile(new File("/Users/mac/Documents/ss.txt"));
            byte[] bytes = uploadUtil.downFile(stringResult.getModule());

        }
//        IOUtils.write(bytes, new FileOutputStream(new File("/Users/mac/new1ss.jpg")));
//        System.out.println(bytes);
    }
}
