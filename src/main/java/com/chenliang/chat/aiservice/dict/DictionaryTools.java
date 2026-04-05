package com.chenliang.chat.aiservice.dict;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典管理工具类。
 * 提供给 AI 助手调用的接口，用于执行具体的业务逻辑。
 */
@Slf4j
@Component
public class DictionaryTools {

    private final DictionaryConfig dictionaryConfig;
    private final RestTemplate restTemplate;

    public DictionaryTools(DictionaryConfig dictionaryConfig, RestTemplate restTemplate) {
        this.dictionaryConfig = dictionaryConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * 保存字典主项信息。
     * 
     * @param dictCode    字典编码，建议使用英文或拼音缩写
     * @param dictName    字典名称，中文描述
     * @param description 字典的功能详细描述
     * @return 接口返回的结果字符串
     */
    @Tool("新增字典主项接口。当你识别到用户想要创建、新增或定义一个新的字典分类/主项时，请调用此工具。你需要根据用户的意图，自动推导出合适的 dictCode (英文编码)、dictName (中文名称) 和 description (详细描述)。")
    public String saveDictInfo(
            @P("字典主项编码 (例如: vehicle_brand, color_type)") String dictCode,
            @P("字典主项名称 (例如: 车辆品牌, 颜色类型)") String dictName,
            @P("字典主项的详细描述") String description) {
        
        log.info("开始调用接口保存字典主项: code={}, name={}", dictCode, dictName);

        try {
            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", dictionaryConfig.getAuthorization());

            // 构造请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dictCode", dictCode);
            requestBody.put("dictName", dictName);
            requestBody.put("description", description);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            String response = restTemplate.postForObject(dictionaryConfig.getSaveUrl(), entity, String.class);
            
            log.info("接口返回结果: {}", response);
            return "成功调用新增字典主项接口。接口原始响应如下: " + response;
            
        } catch (Exception e) {
            log.error("调用字典保存接口失败", e);
            return "调用新增字典主项接口失败，错误信息: " + e.getMessage();
        }
    }

    /**
     * 新增字典子项（详情项）接口。
     * 当识别到用户想要配置具体字典条目、选项、或在创建主项后补全子项时，请调用此工具。
     * 你需要推导出参数并展示表格确认。
     *
     * @param dictCode 字典主项编码 (例如: vehicle_status)
     * @param itemCode 子项编码 (例如: 1, 0, ON, OFF)
     * @param zhCN     中文名称 (例如: 正常, 禁用)
     * @param enUS     英文名称或对应值 (例如: NORMAL, DISABLE)
     * @param itemSort 排序序号 (通常 1, 2, 3...)
     * @return 接口调用结果
     */
    @Tool("新增字典子项接口。用于在现有字典主项下增加具体的选项值。要求参数：所属主项编码、子项编码、中文名、英文名、排序号。")
    public String saveDictItem(
            @P("所属字典主项的编码") String dictCode,
            @P("子项编码 (如: 1)") String itemCode,
            @P("中文名称") String zhCN,
            @P("英文名称或值") String enUS,
            @P("排序号") Integer itemSort) {

        log.info("开始调用接口保存字典子项: dictCode={}, itemCode={}, zhCN={}", dictCode, itemCode, zhCN);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", dictionaryConfig.getAuthorization());

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dictCode", dictCode);
            requestBody.put("itemCode", itemCode);
            requestBody.put("zh-CN", zhCN);
            requestBody.put("en-US", enUS);
            requestBody.put("itemSort", itemSort.toString());
            requestBody.put("styleColor", "");
            // 拼接语言包数据格式
            requestBody.put("languageData", String.format("zh-CN|%s&en-US|%s", zhCN, enUS));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(dictionaryConfig.getItemSaveUrl(), entity, String.class);

            log.info("子项接口返回结果: {}", response);
            return "成功调用新增字典子项 [" + zhCN + "]。接口响应: " + response;

        } catch (Exception e) {
            log.error("调用字典子项保存接口失败", e);
            return "调用新增字典子项 [" + zhCN + "] 失败: " + e.getMessage();
        }
    }
}
