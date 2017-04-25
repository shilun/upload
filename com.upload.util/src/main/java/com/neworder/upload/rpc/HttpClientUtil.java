package com.neworder.upload.rpc;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.common.exception.ApplicationException;
import com.common.security.MD5;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;


public class HttpClientUtil {

	private static final Logger LOGGER = Logger.getLogger(HttpClientUtil.class);

	private String authKey;

	private String getHeader() {
		if (StringUtils.isNotBlank(authKey)) {
			MD5 md5 = new MD5();
			String header = MD5.MD5Str(authKey);
			return header;
		}
		return null;
	}

	/***
	 * 获取post请求主体内容
	 *
	 * @param url
	 * @param nvps
	 * @return
	 */
	public byte[] doGet(String url, List<NameValuePair> nvps) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		InputStream content = null;
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader(new BasicHeader("authKey", getHeader()));
			if (!url.endsWith("?")) {
				url = url + "?";
			}
			URI uri = new URI(url + URLEncodedUtils.format(nvps, "UTF-8"));
			httpget.setURI(uri);
			HttpResponse response = httpClient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				LOGGER.error("发送服务请求失败");
				return null;
			}
			HttpEntity entity = response.getEntity();

			content = entity.getContent();
			return IOUtils.toByteArray(content);
		} catch (Exception e) {
			for (NameValuePair key : nvps) {
				LOGGER.error(key.getName() + ":" + key.getValue());
			}
			LOGGER.error("发送服务请求失败", e);
		} finally {
			IOUtils.closeQuietly(content);
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public byte[] doGet(String url, Map<String, String> params) {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key)));
		}
		byte[] result = doGet(url, nvps);
		return result;

	}

	public JSONObject doGetJSON(String url, Map<String, String> params) {
		byte[] result = null;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				nvps.add(new BasicNameValuePair(key, params.get(key)));
			}
			result = doGet(url, nvps);
			JSONObject jsonObject = JSONObject.fromObject(new String(result));
			return jsonObject;
		} catch (Exception e) {
			LOGGER.error("执行远程httpGet请求出错");
			LOGGER.error("url->:" + url);
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				LOGGER.error(key + ":" + params.get(key));
			}
			if (result != null) {
				LOGGER.error("http response:->" + new String(result));
			}
			throw new ApplicationException("执行远程httpGet请求出错", e);
		}

	}

	public byte[] doPostByte(String url, Map<String, Object> params) {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpPost post = new HttpPost(url);
		post.setHeader(new BasicHeader("authKey", getHeader()));
		Set<String> keySet = params.keySet();
		MultipartEntity builder = new MultipartEntity();
		InputStream content = null;
		try {
			for (String key : keySet) {
				Object object = params.get(key);
				if (object instanceof byte[]) {
					ByteArrayBody array = new ByteArrayBody((byte[]) object, key);
					builder.addPart(key, array);
					continue;
				}
				if (object instanceof File) {
					builder.addPart(key, new FileBody((File) object));
					continue;
				}
				if (object instanceof InputStream) {
					throw new ApplicationException("文件post不能以InputStream方传");
				}
				if (object instanceof Boolean) {
					Boolean value = (Boolean) object;
					String str = null;
					if (value == true) {
						str = "true";
					} else {
						str = "false";
					}
					StringBody body = new StringBody(str);
					builder.addPart(key, body);
					continue;
				}
				if (object instanceof Number || object instanceof String) {
					StringBody body = null;
					if (object instanceof String) {
						body = new StringBody(object.toString(), Charset.forName("utf-8"));
					} else {
						body = new StringBody(object.toString());
					}

					builder.addPart(key, body);
					continue;
				}
				throw new ApplicationException("发送post 请求失败 类型只能为 byte[],String,number");
			}
			post.setEntity(builder);
			HttpResponse execute = httpClient.execute(post);
			int statusCode = execute.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				LOGGER.error("发送服务请求失败statusCode:" + statusCode);
				throw new ApplicationException("执行远程httpPost请求出错 页面返回为非statusCode:" + statusCode);
			}
			HttpEntity entity = execute.getEntity();
			content = entity.getContent();
			return IOUtils.toByteArray(content);
		} catch (Exception e) {
			LOGGER.error("执行远程httpPost请求出错");
			LOGGER.error("url->:" + url);
			throw new ApplicationException("执行远程httpPost请求出错", e);
		} finally {
			IOUtils.closeQuietly(content);
			httpClient.getConnectionManager().shutdown();
		}
	}

	public JSONObject doPost(String url, Map<String, Object> params) {
		String content = new String(doPostByte(url, params));
		JSONObject object = JSONObject.fromObject(content);
		return object;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

}