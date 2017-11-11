package com.upload.rpc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.common.exception.BizException;
import com.common.util.Result;
import com.common.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;



public class UploadUtil {
	private static final Logger LOGGER = Logger.getLogger(UploadUtil.class);
	// 域名
	private String domainName;
	// 业务碼
	private String code;
	// 简码
	private String scode;
	// 客户端
	private HttpClientUtil clientUtil = new HttpClientUtil();

	private static Properties props = System.getProperties();

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getScode() {
		return scode;
	}

	public void setScode(String scode) {
		this.scode = scode;
	}

	/**
	 * 上传文件
	 *
	 * @param file
	 *            要上传的文件
	 * @return
	 */
	public Result<String> uploadFile(File file) {
		Result<String> result = new Result<String>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("file", file);
		params.put("key", code);

		JSONObject doPost = clientUtil.doPost("http://" + domainName + "/upload", params);

		result.setSuccess(doPost.getBoolean("success"));
		if (!doPost.getBoolean("success")) {
			throw new BizException(doPost.getString("code"), doPost.getString("message"));
		}
		if (doPost.containsKey("message")) {
			result.setMessage(doPost.getString("message"));
		}
		result.setModule(doPost.getString("data"));

		return result;
	}

	/**
	 * 文件上传
	 *
	 * @param fileByte
	 *            文件二进制流
	 * @param name
	 *            文件名
	 * @return
	 */
	public Result<String> uploadFile(byte[] fileByte, String name) {
		String temp_path = props.getProperty("java.io.tmpdir");
		temp_path = temp_path + "/" + StringUtils.getUUID();
		File tempDir = new File(temp_path);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		FileOutputStream output = null;
		try {
			String fileName = temp_path + "/" + name;
			output = new FileOutputStream(fileName);
			IOUtils.write(fileByte, output);
			File uploadFile = new File(fileName);
			return uploadFile(uploadFile);
		} catch (Exception e) {
			LOGGER.error("文件上传失败", e);
			throw new BizException("文件上传失败");
		} finally {
			IOUtils.closeQuietly(output);
			tempDir.delete();
		}
	}

	/**
	 * 文件下载
	 *
	 * @param fileName
	 *            文件
	 * @return
	 */
	public byte[] downFile(String fileName) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("key", code);
		params.put("fileName", fileName);
		return clientUtil.doPostByte("http://" + domainName + "/download", params);
	}
}
