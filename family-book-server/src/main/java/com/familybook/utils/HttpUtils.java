package com.familybook.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class HttpUtils {

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject get(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return JSON.parseObject(response.getBody());
        } catch (Exception e) {
            log.error("HTTP GET request failed: {}", url, e);
            return null;
        }
    }
}
