package com.upload.service.impl;


import com.common.exception.ApplicationException;
import com.common.util.Result;
import com.upload.util.constants.SystemConstants;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

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
    private Logger logger = Logger.getLogger(ImageProcessor.class);

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
     *
     * @param targetFile
     * @param aImageSize
     * @return
     */
    public Result saveImage(File targetFile, String[] aImageSize) {
        Result result = new Result();
        String[] tmpDefaultSize = systemConstants.getDefaultImageSize().split(",");
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
            logger.error("图片保存大小",e);
            throw new ApplicationException("文件上传失败,请上传图片文件", e);
        }
        /**
         * 存放原图
         */

        tmpFlag = resize(tmpImg, width, height, targetFile.getPath());

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
                    String prefix=tempFile.substring(tempFile.lastIndexOf("."));
                    tempFile=tempFile.substring(0,tempFile.lastIndexOf("."));
                    tempFile=tempFile+"_"+tmpResizeWidth+"x"+tmpResizeHeight;
                    tempFile=tempFile+prefix;
                    if (width > height) {
                        // 以宽度为基准，等比例放缩图片
                        int tmpHeight = (int) (height * tmpResizeWidth / width);
                        tmpFlag = resize(tmpImg, tmpResizeWidth, tmpHeight, tempFile);
                    } else {
                        // 以高度为基准，等比例缩放图片
                        int tmpWidth = (int) (width * tmpResizeHeight / height);
                        tmpFlag = resize(tmpImg, tmpWidth, tmpResizeHeight, tempFile);
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
     * @param aImage
     * @param aWidth
     * @param aHeight
     * @return
     */
    public Result resize(Image aImage, int aWidth, int aHeight, String taskFile) {
        Result result = new Result();
        // SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
        BufferedImage image = new BufferedImage(aWidth, aHeight, BufferedImage.TYPE_USHORT_565_RGB);
        image.getGraphics().drawImage(aImage, 0, 0, aWidth, aHeight, null); // 绘制缩小后的图
        File destFile = new File(taskFile);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destFile);
            ImageIO.write(image, JPG, out);
            result.addDefaultModel("size", destFile.length());
        } catch (IOException e) {
            IOUtils.closeQuietly(out);
            logger.error("上传文件失", e);
            result.setSuccess(false);
            result.setResultCode(e.getMessage());
            throw new ApplicationException("文件上传失败", e);
        } finally {
            IOUtils.closeQuietly(out);
            if (this.storyAliyun) {
                destFile.delete();
            }
        }
        result.setSuccess(true);
        return result;
    }




}
