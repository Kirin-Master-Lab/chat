package com.chenliang.chat.aiservice.dict;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 字典接口配置类。
 * 从 application.properties 中读取 dict.api.* 相关的配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dict.api")
public class DictionaryConfig {

    /**
     * 保存字典主项的接口 URL。
     */
    private String saveUrl;

    /**
     * 接口调用所需的 Authorization Token。
     */
    private String authorization;

    /**
     * 保存从 application.properties 读取的子项接口 URL。
     */
    private String itemSaveUrl;
}
