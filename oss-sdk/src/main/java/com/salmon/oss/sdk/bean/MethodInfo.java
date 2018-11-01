package com.salmon.oss.sdk.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * 方法调用信息类
 */
@Data
@NoArgsConstructor
public class MethodInfo {

    /**
     * 请求url
     */
    private String url;

    /**
     * 请求方法
     */
    private HttpMethod method;

    private boolean isList;

    /**
     * PathVariable
     */
    private Map<String, Object> pathParams;
    /**
     * 请求参数 RequestParam
     */
    private MultiValueMap<String, Object> requestParams;
    /**
     * 返回对象的类型
     */
    private Class<?> returnElementType;
}
