package com.example.demo.notification.SendProcessor;

import com.example.demo.notification.SendProcessor.proxy.SocketHandler;
import com.example.demo.service.SocketService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Proxy;

public class SendProcessor implements BeanPostProcessor, BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Value("${websocket.url}")
    private String url;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof SocketService){
            Address address = new Address(url);
            return Proxy.newProxyInstance(classLoader,new Class[]{bean.getClass()},new SocketHandler(bean, address));
        }

        return bean;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
