package com.salmon.oss.sdk.interfaces;

import com.salmon.oss.sdk.bean.MethodInfo;
import com.salmon.oss.sdk.bean.ServerInfo;

import java.io.File;
import java.io.IOException;

/**
 * REST 请求调用类
 */
public interface RestHandler {

    /**
     * 初始化服务器信息
     * @param serverInfo 服务器信息
     */
    void init(ServerInfo serverInfo);

    /**
     * 调用RESTFUL API
     * @param methodInfo  方法信息
     * @return 返回调用结果
     */
    Object invokeRest(MethodInfo methodInfo) throws IOException;


    Object download(MethodInfo methodInfo) throws IOException;


    Object upload(MethodInfo methodInfo) throws IOException;



}
