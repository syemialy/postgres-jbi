package com.jetbi.postgresftsapp.spring.stereotype.service.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Sergei.Emelianov on 24.03.2016.
 */
@Controller
public class UtilitiesController {

    @Autowired private Environment env;

    @Autowired private Gson gson;

    @RequestMapping(value = "dev/environment", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String readEnv() {
        Properties props = System.getProperties();
        Map<String,Object> prs = new HashMap<>(1);
        for ( String key: props.stringPropertyNames()) {
           prs.put(key,props.get(key));
        }
        return gson.toJson(prs);
    }
}
