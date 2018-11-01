package com.salmon.oss.sdk.impl;

import com.salmon.oss.sdk.bean.MethodInfo;
import com.salmon.oss.sdk.bean.ServerInfo;
import com.salmon.oss.sdk.interfaces.ProxyCreate;
import com.salmon.oss.sdk.interfaces.RestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JdkProxyCreate implements ProxyCreate {

    /**
     * 创建代理类
     *
     * @param type 原始类类型
     * @return 代理的对象
     */
    @Override
    public Object createProxy(Class<?> type) {
        log.info("create impl " + type);
        //给每一个代理类一个实现
        RestHandler handler = new RestHandlerImpl();
        // InvocationHandler  中的 Object invoke(proxy, method, args)
        // proxy 被代理的对象 method 被代理对象的方法 args 方法的参数 返回值Object 为调用方法返回的结果
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
                    if (method.getName().equals("init")) { // 调用初始化方法
                        //根据接口获取服务信息
                        ServerInfo serverInfo = extractServerInfo(args);
                        //初始化服务器信息
                        handler.init(serverInfo);
                    } else {
                        //根据方法和参数得到调用信息
                        MethodInfo methodInfo = extractMethodInfo(method, args);
                        if (method.getName().equals("getObject")) {//下载文件
                            return handler.download(methodInfo);
                        } else if (method.getName().equals("putObject")) {
                            return handler.upload(methodInfo);
                        } else {
                            return handler.invokeRest(methodInfo);
                        }
                    }
                    return null;
                }
        );
    }

    private ServerInfo extractServerInfo(Object[] args) {
        ServerInfo serverInfo = new ServerInfo();
        // 为方便扩展，API server 的地址 从配置文件中获取
        serverInfo.setUrl(String.valueOf(args[0]));
        serverInfo.setToken(String.valueOf(args[1]));
        return serverInfo;
    }

    /**
     * 根据方法定义和调用参数得到调用的相关信息
     *
     * @param method
     * @param args
     * @return
     */
    private MethodInfo extractMethodInfo(Method method, Object[] args) {
        MethodInfo methodInfo = new MethodInfo();
        extractUrlAndMethod(method, methodInfo);
        extractRequestParamAndBody(method, args, methodInfo);
        // 提取返回对象信息
        extractReturnInfo(method, methodInfo);
        if(methodInfo.getMethod() == HttpMethod.GET) {
            StringBuilder url = new StringBuilder(methodInfo.getUrl());
            Map<String,Object> params = methodInfo.getRequestParams().toSingleValueMap();
            params.keySet().forEach(k -> {
                if(url.toString().length() == methodInfo.getUrl().length())
                    url.append("?");
                else
                    url.append("&");
                url.append(k).append("=").append("{").append(k).append("}");
            });
            methodInfo.setUrl(url.toString());
            methodInfo.getPathParams().putAll(params);
        }
        return methodInfo;
    }

    /**
     * 提取返回对象信息
     *
     * @param method
     * @param methodInfo
     */
    private void extractReturnInfo(Method method, MethodInfo methodInfo) {
        boolean isList = method.getReturnType().isAssignableFrom(List.class);
        methodInfo.setList(isList);
        Class<?> elementType = extractElementType(method.getGenericReturnType());
        if (elementType == null)
            methodInfo.setReturnElementType(method.getReturnType());
        else
            methodInfo.setReturnElementType(elementType);
    }

    /**
     * 得到泛型类型的实际类型
     *
     * @param genericReturnType 泛型类型
     * @return
     */
    private Class<?> extractElementType(Type genericReturnType) {
        if(genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return (Class<?>) actualTypeArguments[0];
        } else {
            return null;
        }
    }

    /**
     * 得到请求的param和body
     *
     * @param method
     * @param args
     * @param methodInfo
     */
    private void extractRequestParamAndBody(Method method, Object[] args, MethodInfo methodInfo) {
        // 得到调用的参数和body
        Parameter[] parameters = method.getParameters();
        // 参数和值对应的map
        Map<String, Object> pathParams = new HashMap<>();
        methodInfo.setPathParams(pathParams);

        MultiValueMap<String, Object> requestParams = new LinkedMultiValueMap<>();
        methodInfo.setRequestParams(requestParams);

        for (int i = 0; i < parameters.length; i++) {
            // 是否带 @PathVariable
            PathVariable annoPath = parameters[i].getAnnotation(PathVariable.class);
            if (annoPath != null) {
                pathParams.put(StringUtils.isEmpty(annoPath.value())? annoPath.name() : annoPath.value(), args[i]);
            }

            RequestParam annoReqParam = parameters[i].getAnnotation(RequestParam.class);
            if (annoReqParam != null) {
                requestParams.add(StringUtils.isEmpty(annoReqParam.value())? annoReqParam.name() : annoReqParam.value(), args[i]);
            }

            // 是否带了 RequestBody
            RequestBody annoBody = parameters[i].getAnnotation(RequestBody.class);
            if (annoBody != null) {
            }
        }
    }

    /**
     * 得到请求的URL和方法
     *
     * @param method
     * @param methodInfo
     */
    private void extractUrlAndMethod(Method method, MethodInfo methodInfo) {
        // 得到请求URL和请求方法
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            // GET
            if (annotation instanceof GetMapping) {
                GetMapping a = (GetMapping) annotation;
                methodInfo.setUrl(a.value()[0]);
                methodInfo.setMethod(HttpMethod.GET);
            }
            // POST
            else if (annotation instanceof PostMapping) {
                PostMapping a = (PostMapping) annotation;
                methodInfo.setUrl(a.value()[0]);
                methodInfo.setMethod(HttpMethod.POST);
            }
            // DELETE
            else if (annotation instanceof DeleteMapping) {
                DeleteMapping a = (DeleteMapping) annotation;
                methodInfo.setUrl(a.value()[0]);
                methodInfo.setMethod(HttpMethod.DELETE);
            }
            // PUT
            else if (annotation instanceof PutMapping) {
                PutMapping a = (PutMapping) annotation;
                methodInfo.setUrl(a.value()[0]);
                methodInfo.setMethod(HttpMethod.PUT);
            }
            // PATCH
            else if (annotation instanceof PatchMapping) {
                PatchMapping a = (PatchMapping) annotation;
                methodInfo.setUrl(a.value()[0]);
                methodInfo.setMethod(HttpMethod.PATCH);
            }
        }
    }
}
