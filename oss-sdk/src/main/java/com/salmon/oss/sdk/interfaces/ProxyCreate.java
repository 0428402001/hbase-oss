package com.salmon.oss.sdk.interfaces;

/**
 * 创建代理类接口
 */
public interface ProxyCreate {

    /**
     * 创建代理类
     * @param type 原始类类型
     * @return 代理的对象
     */
    Object createProxy(Class<?> type);
}
