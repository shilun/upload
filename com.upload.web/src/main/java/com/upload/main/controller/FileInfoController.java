package com.upload.main.controller;

import com.common.exception.BizException;
import com.common.web.AbstractController;
import com.common.web.IExecute;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;

@Controller
@RequestMapping(value = {"/"}, method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
public class FileInfoController
        extends AbstractController {


    public static void main(String[] args) {
        byte[] encode = Base64.encodeBase64("001".getBytes());
        String encodes = new String(encode);
        String encode1 = null;
        String result = new String(Base64.decodeBase64(encodes.getBytes()));
        try {
            encode1 = URLEncoder.encode(encodes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(encode1);
    }

    private static final Logger LOGGER = Logger.getLogger(FileInfoController.class);
    @Resource
    private FileInfoService fileInfoService;


    @ResponseBody
    @RequestMapping
    public String index() {
        return "<html><body><video src=\"/video/283ba708bade462e9018cf2b973a9200\" controls=\"controls\">\n" +
                "your browser does not support the video tag\n" +
                "</video></body></html>";
    }

    @RequestMapping({"/download"})
    public void donwload(String fileName, String key, HttpServletResponse response)
            throws Exception {
        try {
            Map<String, Object> downFile = this.fileInfoService.downFile(key, fileName);
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


    private void downloadPlayerIndex(@PathVariable String file, HttpServletResponse response) {
        String path = fileRootPath + "/video/" + file + "/default.m3u8";
        File realFile = new File(path);
        try {
            if (realFile.getAbsoluteFile().exists()) {
                byte[] bytes = IOUtils.toByteArray(new FileInputStream(realFile));
                download(response, bytes, "application/vnd.apple.mpegurl", "default.m3u8");
            }
        } catch (Exception e) {

        }
    }

    @RequestMapping({"/video/{file}/{item}.ts"})
    public void down(@PathVariable String file, @PathVariable String item, HttpServletResponse response)
            throws Exception {
        String path = fileRootPath + "/video/" + file + "/" + item + ".ts";
        File realFile = new File(path);
        if (realFile.exists()) {
            downVideo(path, response);
        }
    }

    @RequestMapping({"{size}/{scode}/{file}.{fileType}"})
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String size, @PathVariable String fileType, String name, HttpServletResponse response)
            throws Exception {
        if (StringUtils.endsWithIgnoreCase("scode", "video")) {
            return;
        }
        try {
            byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, size);
            if (StringUtils.isNotBlank(name)) {
                file = name;
            }
            String typeName = "application/x-download";
            if (this.fileInfoService.getPictures().contains(fileType)) {
                typeName = "jpg";
                file = "";
            } else {
                file = file + "." + fileType;
            }

            download(response, data, typeName, file);
        } catch (BizException e) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            request.getSession().getServletContext().getRequestDispatcher("/download_erro?code=" + e.getCode() + "&message=" + e.getMessage()).forward(request, response);
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

    @RequestMapping({"{scode}/{file}.{fileType}"})
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String fileType, String name, HttpServletResponse response)
            throws Exception {
        try {
            if (fileType.equalsIgnoreCase("m3u8")) {
                downloadPlayerIndex(file, response);
                return;
            }
            if (this.fileInfoService.getPictures().contains(fileType)) {
                byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, "");
                String typeName = "jpg";
                file = "";
                download(response, data, typeName, file);
                return;
            } else {
                file = file + "." + fileType;
                response.setContentType("application/x-download");
                File currentFile = fileInfoService.httpDown(scode, file);
                downloadExistsFile(getRequest(), response, currentFile);
            }
        } catch (BizException e) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            request.getSession().getServletContext().getRequestDispatcher("/download_erro?code=" + e.getCode() + "&message=" + e.getMessage()).forward(request, response);
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

    private void downloadExistsFile(HttpServletRequest request, HttpServletResponse response, File proposeFile) throws Exception,
            FileNotFoundException {
        long fSize = proposeFile.length();
        // 下载
        response.setContentType("application/x-download");
        String isoFileName = proposeFile.getName();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(fSize));
        response.setHeader("Content-Disposition", "attachment; filename="
                + isoFileName);
        long pos = 0;
        if (null != request.getHeader("Range")) {
            // 断点续传
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            pos = Long.parseLong(request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));
        }
        ServletOutputStream out = null;
        BufferedOutputStream bufferOut = null;
        InputStream inputStream = null;
        try {
             out = response.getOutputStream();
             bufferOut = new BufferedOutputStream(out);
             inputStream = new FileInputStream(proposeFile);
            String contentRange = new StringBuffer("bytes ").append(
                    new Long(pos).toString()).append("-").append(
                    new Long(fSize - 1).toString()).append("/").append(
                    new Long(fSize).toString()).toString();
            response.setHeader("Content-Range", contentRange);
            inputStream.skip(pos);
            byte[] buffer = new byte[5 * 1024];
            int length = 0;
            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                bufferOut.write(buffer, 0, length);
            }
            bufferOut.flush();
        }
        catch(Exception e){
            throw e;
        }
        finally {
            IOUtils.closeQuietly(bufferOut);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void download(HttpServletResponse response, byte[] file, String contentType, String realName)
            throws Exception {
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
                return fileInfoService.upload(file, key);
            }
        });
    }
}
