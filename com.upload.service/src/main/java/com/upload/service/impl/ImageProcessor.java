package com.upload.service.impl;


import com.common.exception.ApplicationException;
import com.common.util.Result;
import com.upload.util.constants.SystemConstants;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片存储处理器
 *
 * @author 004850
 */
@Service
public class ImageProcessor {
    /**
     * 图片存储格式
     */
    private static final String JPG = "jpg";

    private boolean storyAliyun = false;

    private String storyAuthKey;

    private String storyAccessKey;

    /**
     * 日志对象
     */
    private Logger logger = LoggerFactory.getLogger(ImageProcessor.class);

    @Resource
    private SystemConstants systemConstants;


    /**
     * 生成带尺寸的文件名
     *
     * @param file      文件名
     * @param aFileSize 文件尺寸
     * @return
     */
    private String tranceFileName(String file, String aFileSize) {
        String tmpFileName = file.substring(0, file.lastIndexOf("."));
        tmpFileName = tmpFileName + "_" + aFileSize + "." + JPG;
        return tmpFileName;
    }

    /**
     * @param targetFile
     * @param tmpDefaultSize
     * @return
     */
    public Result saveImage(File targetFile, String[] tmpDefaultSize) {
        Result result = new Result();
        Image tmpImg = null;
        InputStream tmpIn = null;
        try {
            tmpIn = new FileInputStream(targetFile);
        } catch (Exception e) {
            logger.error("读取图片失败", e);
        }
        Result tmpFlag = null;
        // 得到源图宽
        int width = 0;
        // 得到源图长
        int height = 0;
        try {
            // 构造Image对象
            tmpImg = ImageIO.read(tmpIn);
            // 得到源图宽
            width = tmpImg.getWidth(null);
            // 得到源图长
            height = tmpImg.getHeight(null);
        } catch (Exception e) {
            logger.error("图片保存大小", e);
            throw new ApplicationException("文件上传失败,请上传图片文件", e);
        }

        int fileSize = ((Long) tmpFlag.get("size")).intValue();
        /**
         * 默认尺寸压缩
         */
        if (null != tmpDefaultSize) {
            for (int i = 0; i < tmpDefaultSize.length; i++) {
                String[] tmpSizeStr = tmpDefaultSize[i].split("x");
                if (null != tmpSizeStr && tmpSizeStr.length == 2) {
                    int tmpResizeWidth = Integer.parseInt(tmpSizeStr[0]);
                    int tmpResizeHeight = Integer.parseInt(tmpSizeStr[1]);

                    String tempFile = targetFile.getPath();
                    String prefix = tempFile.substring(tempFile.lastIndexOf("."));
                    tempFile = tempFile.substring(0, tempFile.lastIndexOf("."));
                    tempFile = tempFile + "_" + tmpResizeWidth + "x" + tmpResizeHeight;
                    tempFile = tempFile + prefix;
                    if (width > height) {
                        // 以宽度为基准，等比例放缩图片
                        int tmpHeight = (int) (height * tmpResizeWidth / width);
                        tmpFlag = resize(targetFile, tmpResizeWidth, tmpHeight, tempFile);
                    } else {
                        // 以高度为基准，等比例缩放图片
                        int tmpWidth = (int) (width * tmpResizeHeight / height);
                        tmpFlag = resize(targetFile, tmpWidth, tmpResizeHeight, tempFile);
                    }
                    if (!tmpFlag.isSuccess()) {
                        result.setSuccess(false);
                        return result;
                    } else {
                        fileSize = fileSize + ((Long) tmpFlag.get("size")).intValue();
                    }
                }
            }
        }

        IOUtils.closeQuietly(tmpIn);
        result.addDefaultModel("size", fileSize);
        result.setSuccess(true);
        return result;
    }

    /**
     * @param targetFile
     * @return
     */
    public Result saveImage(File targetFile, int maxWidth, int maxHeight) {
        Result result = new Result();
        Image tmpImg = null;

        InputStream tmpIn = null;
        try {
            tmpIn = new FileInputStream(targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Result tmpFlag = null;
        // 得到源图宽
        int width = 0;
        // 得到源图长
        int height = 0;
        try {
            // 构造Image对象
            tmpImg = ImageIO.read(tmpIn);
            // 得到源图宽
            width = tmpImg.getWidth(null);
            // 得到源图长
            height = tmpImg.getHeight(null);
        } catch (Exception e) {
            logger.error("图片保存大小", e);
            IOUtils.closeQuietly(tmpIn);
            throw new ApplicationException("文件上传失败,请上传图片文件", e);
        }
        int tmpResizeWidth = maxWidth;
        int tmpResizeHeight = maxHeight;

        String tempFile = targetFile.getPath();
        String prefix = tempFile.substring(tempFile.lastIndexOf("."));
        tempFile = tempFile.substring(0, tempFile.lastIndexOf("."));
        tempFile = tempFile + "_" + tmpResizeWidth + "x" + tmpResizeHeight;
        tempFile = tempFile + prefix;
        if (width > height) {
            // 以宽度为基准，等比例放缩图片
            int tmpHeight = (int) (height * tmpResizeWidth / width);
            if (tmpHeight < height) {
                tmpHeight = height;
                tmpResizeWidth = width;
            }
            tmpFlag = resize(targetFile, tmpResizeWidth, tmpHeight, tempFile);
        } else {
            // 以高度为基准，等比例缩放图片
            int tmpWidth = (int) (width * tmpResizeHeight / height);
            if (tmpWidth < width) {
                tmpResizeHeight = height;
                tmpWidth = width;
            }
            tmpFlag = resize(targetFile, tmpWidth, tmpResizeHeight, tempFile);
        }
        int fileSize = ((Long) tmpFlag.get("size")).intValue();
        if (!tmpFlag.isSuccess()) {
            result.setSuccess(false);
            return result;
        } else {
            fileSize = fileSize + ((Long) tmpFlag.get("size")).intValue();
        }

        IOUtils.closeQuietly(tmpIn);
        result.addDefaultModel("size", fileSize);
        result.setSuccess(true);
        return result;
    }

    /**
     * @param sourceFile
     * @param aWidth
     * @param aHeight
     * @return
     */
    public Result resize(File sourceFile, int aWidth, int aHeight, String taskFile) {
        Result result = new Result();
        // SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
        File destFile = new File(taskFile);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        if (aWidth == 400) {
            aWidth = 800;
            aHeight = 800;
        }
        try {
            Thumbnails.of(sourceFile).size(aWidth, aHeight).toFile(taskFile);
            result.addDefaultModel("size", destFile.length());
        } catch (IOException e) {
            logger.error("上传文件失", e);
            result.setSuccess(false);
            result.setResultCode(e.getMessage());
            throw new ApplicationException("文件上传失败", e);
        } finally {
            if (this.storyAliyun) {
                destFile.delete();
            }
        }
        result.setSuccess(true);
        return result;
    }


}
