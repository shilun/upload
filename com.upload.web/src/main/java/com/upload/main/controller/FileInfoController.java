package com.upload.main.controller;

import com.common.exception.BizException;
import com.common.web.AbstractController;
import com.common.web.IExecute;
import com.upload.domain.model.FileTypeEnum;
import com.upload.service.FileInfoService;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = {"/"}, method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
public class FileInfoController
        extends AbstractController {
    @Resource
    private FileInfoService fileInfoService;

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

    @RequestMapping({"{size}/{scode}/{file}.{fileType}"})
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String size, @PathVariable String fileType, String name, HttpServletResponse response)
            throws Exception {
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

    @RequestMapping({"{scode}/{file}.{fileType}"})
    public void down(@PathVariable String scode, @PathVariable String file, @PathVariable String fileType, String name, HttpServletResponse response)
            throws Exception {
        try {
            byte[] data = this.fileInfoService.httpDown(scode, file + "." + fileType, "");
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

    @RequestMapping({"/download_erro"})
    @ResponseBody
    public Map<String, Object> downError(final String code, final String message) {
        return buildMessage(new IExecute() {
            public Object getData() {
                throw new BizException(code, message);
            }
        });
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
                return FileInfoController.this.fileInfoService.upload(file, key);
            }
        });
    }
}
