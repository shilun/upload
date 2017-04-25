package com.upload.main.controller;

import com.common.exception.ApplicationException;
import com.common.util.IGlossary;
import com.common.util.StringUtils;
import com.common.util.model.SexEnum;
import com.common.util.model.YesOrNoEnum;
import com.common.web.AbstractController;
import com.common.web.IExecute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/glossery"})
public class GlossaryController
        extends AbstractController {
    private static Map<String, Class> glosseryItems = new HashMap();

    static {
        glosseryItems.put("yesOrNo", YesOrNoEnum.class);
        glosseryItems.put("sex", SexEnum.class);
    }

    @RequestMapping({"/buildGlossery"})
    @ResponseBody
    public Map<String, Object> buildGlossery(final String type) {
        return buildMessage(new IExecute() {
            public Object getData() {
                List<Map<String, Object>> keyValues = new ArrayList();
                if (StringUtils.isBlank(type)) {
                    throw new ApplicationException("buildGlossery Error unKnow type");
                }
                Class currentEnum = (Class) GlossaryController.glosseryItems.get(type);
                for (Object o : currentEnum.getEnumConstants()) {
                    IGlossary v = (IGlossary) o;
                    HashMap<String, Object> item = new HashMap();
                    item.put("value", v.getValue());
                    item.put("name", v.getName());
                    keyValues.add(item);
                }
                return keyValues;
            }
        });
    }
}
