package com.upload.main.controller;

import com.common.exception.BizException;
import com.common.httpclient.HttpClientUtil;
import com.common.upload.UploadUtil;
import com.common.util.Result;
import com.common.util.model.YesOrNoEnum;
import com.common.web.AbstractController;
import com.common.web.IExecute;
import com.upload.domain.FileUploadConfig;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;
import com.upload.service.FileUploadConfigService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@RequestMapping(value = {"/"}, method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST, RequestMethod.OPTIONS})
public class FileInfoController extends AbstractController {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileInfoController.class);
    @Resource
    private FileInfoService fileInfoService;


    @RequestMapping({"/download"})
    public void donwload(String fileName, String key, HttpServletResponse response) throws Exception {
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
                download(response, bytes, "application/vnd.apple.mpegurl", file + ".m3u8");
            }
        } catch (Exception e) {
            LOGGER.error("downloadPlayerIndex.error", e);
        }
    }

    @RequestMapping({"/video/{file}/{item}.ts"})
    public void down(@PathVariable String file, @PathVariable String item, HttpServletResponse response) throws Exception {
        String path = fileRootPath + "/video/" + file + "/" + item + ".ts";
        File realFile = new File(path);
        if (realFile.exists()) {
            downVideo(path, response);
        }
    }

    @RequestMapping({"{size}/{scode}/{file}.{fileType}"})
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String size, @PathVariable String fileType, String name, HttpServletResponse response) throws Exception {
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
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String fileType, String name, HttpServletResponse response) throws Exception {
        try {
            if (fileType.equalsIgnoreCase("m3u8")) {
                downloadPlayerIndex(file, response);
                return;
            }
            FileUploadConfig configByScode = fileInfoService.findConfigByScode(scode);
            if (configByScode.getHttpDown().intValue() == YesOrNoEnum.NO.getValue()) {
                throw new BizException("data.error", "非法操作");
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

    private void downloadExistsFile(HttpServletRequest request, HttpServletResponse response, File proposeFile) throws Exception, FileNotFoundException {
        java.io.FileInputStream fis = new java.io.FileInputStream(proposeFile);
        response.reset();
        //告诉客户端允许断点续传多线程连接下载
        //响应的格式是:
        //Accept-Ranges: bytes
        response.setHeader("Accept-Ranges", "bytes");

        long p = 0;
        long l = 0;
        //l = raf.length();
        l = proposeFile.length();

        //如果是第一次下,还没有断点续传,状态是默认的 200,无需显式设置
        //响应的格式是:
        //HTTP/1.1 200 OK

        String range = request.getHeader("Range");
        if (range != null) //客户端请求的下载的文件块的开始字节
        {
            //如果是下载文件的范围而不是全部,向客户端声明支持并开始文件块下载
            //要设置状态
            //响应的格式是:
            //HTTP/1.1 206 Partial Content
            if (range.equalsIgnoreCase("bytes=0-")) {
                response.setStatus(HttpServletResponse.SC_OK);//206
            } else {
                response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
            }

            //从请求中得到开始的字节
            //请求的格式是:
            //Range: bytes=[文件块的开始字节]-
//            p = Long.parseLong(request.getHeader("Range").replaceAll("bytes=","").replaceAll("-",""));
            p = Long.parseLong((range.replaceAll("bytes=", "").split("-")[0]));
        }

        //下载的文件(或块)长度
        //响应的格式是:
        //Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
        response.setHeader("Content-Length", new Long(l - p).toString());

        if (p != 0) {
            //不是从最开始下载,
            //响应的格式是:
            //Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
            response.setHeader("Content-Range", "bytes " + new Long(p).toString() + "-" + new Long(l - 1).toString() + "/" + new Long(l).toString());
        }

        //response.setHeader("Connection", "Close"); //如果有此句话不能用 IE 直接下载

        //使客户端直接下载
        //响应的格式是:
        //Content-Type: application/octet-stream
        response.setContentType("application/octet-stream");

        //为客户端下载指定默认的下载文件名称
        //响应的格式是:
        //Content-Disposition: attachment;filename="[文件名]"
        //response.setHeader("Content-Disposition", "attachment;filename=/"" + s.substring(s.lastIndexOf("//") + 1) + "/""); //经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
        response.setHeader("Content-Disposition", "attachment;filename=\"" + proposeFile.getName() + "\"");
        //raf.seek(p);
        fis.skip(p);
        byte[] b = new byte[1024];
        int i;
        //while ( (i = raf.read(b)) != -1 ) //经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
        while ((i = fis.read(b)) != -1) {
            response.getOutputStream().write(b, 0, i);
        }
        //raf.close();//经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
        fis.close();
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
                return fileInfoService.upload(file, key);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        UploadUtil uploadUtil = new UploadUtil();
        uploadUtil.setDomainName("192.168.0.62:8081");
        uploadUtil.setScode("img");
        uploadUtil.setCode("88c0c97d2983479597130e1c96a25115");
        Result<String> stringResult = uploadUtil.uploadFile(new File("D:\\workspace-movie\\视频\\切图\\mipmap-hdpi\\banner.png"));
        byte[] bytes = uploadUtil.downFile(stringResult.getModule());


        IOUtils.write(bytes, new FileOutputStream(new File("d:/ssss.jpeg")));
        System.out.println(stringResult);
    }

}
