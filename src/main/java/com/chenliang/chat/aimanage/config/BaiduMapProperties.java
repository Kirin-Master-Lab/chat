package com.chenliang.chat.aimanage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.map")
public class BaiduMapProperties {

    private String ak;

    private String placeSearchUrl = "https://api.map.baidu.com/place/v2/search";

    private String geocodingUrl = "https://api.map.baidu.com/geocoding/v3";

    private String directionLiteUrl = "https://api.map.baidu.com/directionlite/v1";

    private String staticMapUrl = "https://api.map.baidu.com/staticimage/v2";
}
