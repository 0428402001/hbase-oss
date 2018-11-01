package com.salmon.oss.sdk;

import com.salmon.oss.sdk.interfaces.ProxyCreate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * 使用 FactoryBean 对自己定义的接口进行容器注册
 */
@Component
public class WebClientFactoryBean implements FactoryBean<WebClientSdk> {

    private ProxyCreate proxyCreate;

    public WebClientFactoryBean(ProxyCreate proxyCreate) {
        this.proxyCreate = proxyCreate;
    }

    /**
     * 返回代理对象
     * @return
     * @throws Exception
     */
    @Override
    public WebClientSdk getObject() throws Exception {
        return (WebClientSdk)this.proxyCreate.createProxy(this.getObjectType());
    }

    @Override
    public Class<?> getObjectType() {
        return WebClientSdk.class;
    }
}
