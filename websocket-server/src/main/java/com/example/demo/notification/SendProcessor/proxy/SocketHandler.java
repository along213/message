package com.example.demo.notification.SendProcessor.proxy;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.notification.SendProcessor.Address;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SocketHandler implements InvocationHandler {

    private Object object = new Object();

    private Address addr;

    public SocketHandler(Object o,Address addr){
        this.object = o;
        this.addr = addr;
    }

    public SocketHandler(Address addr) {
        this.addr = addr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<Object,Object> map = new HashMap<>();
        map.put(args[0],args[1]);
        map.put("method",method.getName());
        Object json = JSONObject.toJSON(map);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(addr.getUrl()+"/websocket");
        StringEntity entity = new StringEntity(json.toString(),"UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");
        httpClient.execute(httpPost);
        return addr;
    }
}
