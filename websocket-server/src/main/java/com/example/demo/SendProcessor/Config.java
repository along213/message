package com.example.demo.SendProcessor;

import com.example.demo.SendProcessor.proxy.SocketHandler;
import com.example.demo.service.SendService;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Proxy;

@Configuration
public class Config implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Value("${websocket.url}")
    private String url;

    @Bean
    public SendService sendService(){
        Address address = new Address(url);
        return (SendService) Proxy.newProxyInstance(classLoader,new Class[]{SendService.class},new SocketHandler(address));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}
