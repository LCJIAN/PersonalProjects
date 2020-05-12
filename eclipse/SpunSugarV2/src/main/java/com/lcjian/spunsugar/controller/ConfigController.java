package com.lcjian.spunsugar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lcjian.spunsugar.entity.Config;
import com.lcjian.spunsugar.service.ConfigService;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, String> getConfigs() {
        List<Config> configs = configService.getConfigs();
        Map<String, String> result = new HashMap<>();
        for (Config config : configs) {
            result.put(config.getKey(), config.getValue());
        }
        return result;
    }

}
