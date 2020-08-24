package com.upload.main.controller;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@Slf4j
public class CollBackController {
    @RequestMapping("/transCallBack")
    void callBack(Map<String, Object> params) {
        log.error(JSON.toJSON(params).toString());
    }
}
