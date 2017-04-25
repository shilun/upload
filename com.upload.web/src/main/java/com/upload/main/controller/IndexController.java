package com.upload.main.controller;

import com.common.web.AbstractController;
import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/"})
public class IndexController
  extends AbstractController
{
  protected static final Long ver = Long.valueOf(new Date().getTime() / 1000L);
  
  @RequestMapping
  public String index()
  {
    tovm("ver", ver);
    return "default";
  }
}
