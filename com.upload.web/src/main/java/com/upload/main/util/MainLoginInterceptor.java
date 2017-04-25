package com.upload.main.util;

import com.common.cookie.CookieUtils;
import com.common.cookie.LoginContext;
import com.common.web.interceptor.LoginContextInterceptor;
import com.common.web.url.JdUrl;
import java.net.MalformedURLException;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.method.HandlerMethod;

public class MainLoginInterceptor
  extends LoginContextInterceptor
{
  private static final Log log = LogFactory.getLog(MainLoginInterceptor.class);
  protected CookieUtils cookieUtils;
  @Resource(name="loginCookieKey")
  protected String loginCookieKey;
  private String loginUrl;
  private JdUrl homeModule;
  
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws Exception
  {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    LoginContext ticket = LoginContext.getTicket();
    if (ticket == null) {
      if (this.loginCookieKey != null) {
        try
        {
          String value = this.cookieUtils.getCookieValue(request, this.loginCookieKey);
          if (StringUtils.isNotBlank(value))
          {
            LoginContext context = getLoginContext(value);
            if (context != null)
            {
              Date minDateTime = context.getCreateTime();
              
              Date maxCreateTime = DateUtils.addMinutes(minDateTime, 40);
              Date currentDate = new Date();
              boolean cookieOk = false;
              if (currentDate.before(maxCreateTime))
              {
                context.setCreateTime(currentDate);
                this.cookieUtils.setCookie(response, this.loginCookieKey, context.toString());
                cookieOk = true;
              }
              if (cookieOk) {
                LoginContext.setTicket(context);
              }
            }
          }
        }
        catch (Exception e)
        {
          log.error("login intercept error", e);
        }
      } else {
        log.debug("session cookie key is empty!");
      }
    }
    return true;
  }
  
  public void setLoginUrl(String loginUrl)
  {
    this.loginUrl = loginUrl;
  }
  
  private String getLoginUrl(String returnurl)
    throws MalformedURLException
  {
    JdUrl url = new JdUrl();
    url.setUrl(this.loginUrl);
    url.addQueryData("returnUrl", returnurl);
    return url.toString();
  }
  
  protected LoginContext getLoginContext(String cookieValue)
  {
    return LoginContext.getTicket(cookieValue);
  }
  
  public void setCookieUtils(CookieUtils cookieUtils)
  {
    this.cookieUtils = cookieUtils;
  }
  
  public void setLoginCookieKey(String loginCookieKey)
  {
    this.loginCookieKey = loginCookieKey;
  }
  
  public void setHomeModule(JdUrl homeModule)
  {
    this.homeModule = homeModule;
  }
}
