package com.chenliang.chat.aimanage.tools;

import com.chenliang.chat.aimanage.config.BaiduMapProperties;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class BaiduMapTools {

    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int DEFAULT_STATIC_WIDTH = 800;
    private static final int DEFAULT_STATIC_HEIGHT = 500;
    private static final int DEFAULT_STATIC_ZOOM = 14;

    private final RestTemplate restTemplate;
    private final BaiduMapProperties baiduMapProperties;

    public BaiduMapTools(RestTemplate restTemplate, BaiduMapProperties baiduMapProperties) {
        this.restTemplate = restTemplate;
        this.baiduMapProperties = baiduMapProperties;
    }

    @Tool("百度地图地点检索。适用于按关键词搜索地点、门店、商圈、景点、地铁站等 POI。返回名称、地址、区域和坐标。")
    public String searchPlaces(
            @P("检索关键词，例如：百度大厦、上海虹桥火车站、天安门") String query,
            @P("检索区域，例如：北京、上海；不确定时可填全国") String region,
            @P("返回条数，建议 1 到 10") Integer pageSize) {
        String akError = validateAk();
        if (akError != null) {
            return akError;
        }

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("query", query);
            params.put("region", defaultRegion(region));
            params.put("page_size", normalizePageSize(pageSize));
            params.put("page_num", 0);
            params.put("scope", 2);
            Map<String, Object> response = executeGet(
                    baiduMapProperties.getPlaceSearchUrl(),
                    params
            );
            String error = extractApiError("地点检索", response);
            if (error != null) {
                return error;
            }

            List<Map<String, Object>> results = asMapList(response.get("results"));
            if (results.isEmpty()) {
                return "【地点检索】未找到匹配地点，请尝试补充城市、商圈或更准确的关键词。";
            }

            StringBuilder builder = new StringBuilder("【地点检索成功】返回 ").append(results.size()).append(" 条结果：");
            int index = 1;
            for (Map<String, Object> item : results) {
                Map<String, Object> location = asMap(item.get("location"));
                builder.append("\n")
                        .append(index++)
                        .append(". 名称: ").append(asString(item.get("name")))
                        .append("；地址: ").append(joinNonBlank(
                                asString(item.get("province")),
                                asString(item.get("city")),
                                asString(item.get("area")),
                                asString(item.get("address"))
                        ))
                        .append("；坐标: ").append(formatLocation(location))
                        .append("；uid: ").append(asString(item.get("uid")));
            }
            return builder.toString();
        } catch (Exception exception) {
            log.error("百度地图地点检索失败", exception);
            return "【地点检索异常】" + exception.getMessage();
        }
    }

    @Tool("百度地图地理编码。将结构化地址转换成百度坐标，经常用于路线规划前先把地址转成坐标。")
    public String geocodeAddress(
            @P("结构化地址，例如：北京市海淀区上地十街十号") String address,
            @P("城市名，可为空；当地址不够完整时建议提供，例如：北京") String city) {
        String akError = validateAk();
        if (akError != null) {
            return akError;
        }

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("address", address);
            params.put("city", trimToNull(city));
            Map<String, Object> response = executeGet(
                    baiduMapProperties.getGeocodingUrl(),
                    params
            );
            String error = extractApiError("地理编码", response);
            if (error != null) {
                return error;
            }

            Map<String, Object> result = asMap(response.get("result"));
            Map<String, Object> location = asMap(result.get("location"));
            if (location.isEmpty()) {
                return "【地理编码】未返回坐标，请检查地址是否足够完整。";
            }

            return "【地理编码成功】地址: " + address
                    + "；坐标: " + formatLocation(location)
                    + "；精确度: " + asString(result.get("precise"))
                    + "；可信度: " + asString(result.get("confidence"));
        } catch (Exception exception) {
            log.error("百度地图地理编码失败", exception);
            return "【地理编码异常】" + exception.getMessage();
        }
    }

    @Tool("百度地图逆地理编码。将百度坐标转换成可读地址、商圈和附近语义描述。坐标格式必须是“纬度,经度”。")
    public String reverseGeocode(
            @P("坐标，格式为：纬度,经度，例如：39.915,116.404") String location) {
        String akError = validateAk();
        if (akError != null) {
            return akError;
        }

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("location", location);
            params.put("extensions_poi", 0);
            Map<String, Object> response = executeGet(
                    baiduMapProperties.getGeocodingUrl(),
                    params
            );
            String error = extractApiError("逆地理编码", response);
            if (error != null) {
                return error;
            }

            Map<String, Object> result = asMap(response.get("result"));
            Map<String, Object> addressComponent = asMap(result.get("addressComponent"));
            return "【逆地理编码成功】地址: " + asString(result.get("formatted_address"))
                    + "；语义描述: " + asString(result.get("sematic_description"))
                    + "；商圈: " + asString(result.get("business"))
                    + "；行政区: " + joinNonBlank(
                            asString(addressComponent.get("province")),
                            asString(addressComponent.get("city")),
                            asString(addressComponent.get("district"))
                    );
        } catch (Exception exception) {
            log.error("百度地图逆地理编码失败", exception);
            return "【逆地理编码异常】" + exception.getMessage();
        }
    }

    @Tool("百度地图轻量路线规划。用于驾车、步行、骑行或公交路线查询。起终点坐标格式必须是“纬度,经度”。mode 仅支持 driving、walking、riding、transit。")
    public String planRoute(
            @P("起点坐标，格式为：纬度,经度") String origin,
            @P("终点坐标，格式为：纬度,经度") String destination,
            @P("出行方式：driving、walking、riding、transit") String mode) {
        String akError = validateAk();
        if (akError != null) {
            return akError;
        }

        String normalizedMode = normalizeRouteMode(mode);
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("origin", origin);
            params.put("destination", destination);
            Map<String, Object> response = executeGet(
                    baiduMapProperties.getDirectionLiteUrl() + "/" + normalizedMode,
                    params
            );
            String error = extractApiError("路线规划", response);
            if (error != null) {
                return error;
            }

            Map<String, Object> result = asMap(response.get("result"));
            List<Map<String, Object>> routes = asMapList(result.get("routes"));
            if (routes.isEmpty()) {
                return "【路线规划】没有返回可用路线，请确认起终点坐标是百度坐标格式“纬度,经度”。";
            }

            Map<String, Object> firstRoute = routes.get(0);
            return "【路线规划成功】方式: " + normalizedMode
                    + "；方案数: " + routes.size()
                    + "；首选路线距离(米): " + asString(firstRoute.get("distance"))
                    + "；耗时(秒): " + asString(firstRoute.get("duration"))
                    + "；打车参考: " + summarizeTaxi(result.get("taxi"));
        } catch (Exception exception) {
            log.error("百度地图路线规划失败", exception);
            return "【路线规划异常】" + exception.getMessage();
        }
    }

    @Tool("生成百度静态地图链接。适合把一个地点或多个标记点直接生成可打开的地图图片地址。center 可以是地名或经纬度，markers 为空时默认只显示 center。")
    public String buildStaticMap(
            @P("地图中心点，可填地点名称或经纬度，例如：北京天安门 或 116.404,39.915") String center,
            @P("可选。标记点列表，多个点用竖线分隔，例如：116.404,39.915|116.414,39.925") String markers,
            @P("缩放级别，建议 3 到 18") Integer zoom,
            @P("图片宽度，最大 1024") Integer width,
            @P("图片高度，最大 1024") Integer height) {
        String akError = validateAk();
        if (akError != null) {
            return akError;
        }

        String resolvedCenter = StringUtils.hasText(center) ? center.trim() : "北京";
        String resolvedMarkers = StringUtils.hasText(markers) ? markers.trim() : resolvedCenter;

        String url = UriComponentsBuilder.fromUriString(baiduMapProperties.getStaticMapUrl())
                .queryParam("ak", baiduMapProperties.getAk())
                .queryParam("center", resolvedCenter)
                .queryParam("markers", resolvedMarkers)
                .queryParam("width", normalizeSize(width, DEFAULT_STATIC_WIDTH))
                .queryParam("height", normalizeSize(height, DEFAULT_STATIC_HEIGHT))
                .queryParam("zoom", normalizeZoom(zoom))
                .build()
                .encode()
                .toUriString();

        return "【静态地图链接】" + url;
    }

    private Map<String, Object> executeGet(String url, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("ak", baiduMapProperties.getAk())
                .queryParam("output", "json");

        params.forEach((key, value) -> {
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                builder.queryParam(key, value);
            }
        });

        try {
            return restTemplate.getForObject(builder.build().encode().toUri(), Map.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException("调用百度地图接口失败: " + exception.getMessage(), exception);
        }
    }

    private String extractApiError(String action, Map<String, Object> response) {
        if (response == null) {
            return "【" + action + "】百度地图接口未返回结果。";
        }

        Integer status = asInteger(response.get("status"));
        if (status == null || status == 0) {
            return null;
        }

        String message = firstNonBlank(
                asString(response.get("message")),
                asString(response.get("msg"))
        );
        return "【" + action + "失败】status=" + status + "，message=" + message;
    }

    private String validateAk() {
        if (StringUtils.hasText(baiduMapProperties.getAk())) {
            return null;
        }
        return "【百度地图未配置】请先在配置中设置 baidu.map.ak 或环境变量 BAIDU_MAP_AK。";
    }

    private String defaultRegion(String region) {
        return StringUtils.hasText(region) ? region.trim() : "全国";
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.max(1, Math.min(pageSize, 10));
    }

    private int normalizeSize(Integer size, int defaultValue) {
        if (size == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(size, 1024));
    }

    private int normalizeZoom(Integer zoom) {
        if (zoom == null) {
            return DEFAULT_STATIC_ZOOM;
        }
        return Math.max(3, Math.min(zoom, 18));
    }

    private String normalizeRouteMode(String mode) {
        String value = trimToNull(mode);
        if (value == null) {
            return "driving";
        }

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "walk", "walking" -> "walking";
            case "bike", "bicycle", "riding" -> "riding";
            case "bus", "transit" -> "transit";
            default -> "driving";
        };
    }

    private String summarizeTaxi(Object taxi) {
        Map<String, Object> taxiMap = asMap(taxi);
        if (taxiMap.isEmpty()) {
            return "无";
        }
        return "费用参考=" + asString(taxiMap.get("detail"))
                + "，总价=" + asString(taxiMap.get("total_price"));
    }

    private String formatLocation(Map<String, Object> location) {
        if (location.isEmpty()) {
            return "未知";
        }
        return asString(location.get("lat")) + "," + asString(location.get("lng"));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asMapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                results.add((Map<String, Object>) map);
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String joinNonBlank(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
