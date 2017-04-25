package com.upload.main.controller;

import com.common.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/ui"})
public class UIController
  extends AbstractController
{
  @RequestMapping({"main"})
  public String main()
  {
    return "main";
  }
  
  @RequestMapping({"changePass"})
  public String changePass()
  {
    return "changePass";
  }
  
  @RequestMapping({"loginOut"})
  public String loginOut()
  {
    return "loginOut";
  }
  
  @RequestMapping({"login"})
  public String login()
  {
    return "login";
  }
  
  @RequestMapping({"configView"})
  public String configView()
  {
    return "config/View";
  }
  
  @RequestMapping({"configList"})
  public String configList()
  {
    return "config/list";
  }
}
