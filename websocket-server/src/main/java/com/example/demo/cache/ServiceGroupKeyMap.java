package com.example.demo.cache;

import com.example.demo.Bean.SocketMsg;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service，group 和 session 的映射
 */
public class ServiceGroupKeyMap {

    /**
     * service和group的映射
     */
    private static Map<String,Map<String, Map<String, Session>>> serviceGroupMap = new ConcurrentHashMap<>();

    /**
     * service和key的映射
     */
    private static Map<String,Map<String,Session>> serviceKeySessionMap = new ConcurrentHashMap<>();


    private static final Object o = new Object();

    public synchronized static void save(SocketMsg socketMsg,String key,Session session){
        if (!StringUtils.isEmpty(socketMsg.getGroup()))
        {
            Map<String, Map<String, Session>> serviceGroupMaps = serviceGroupMap.get(socketMsg.getServiceName());
            //如果没有组 新建一个
            if (null == serviceGroupMaps)
            {
                serviceGroupMaps = new ConcurrentHashMap<>();
                Map<String, Session> keySessionMap = new ConcurrentHashMap<>();
                keySessionMap.put(key,session);
                serviceGroupMaps.put(socketMsg.getGroup(),keySessionMap);
                serviceGroupMap.put(socketMsg.getServiceName(),serviceGroupMaps);
            }
            else
            {
                setMap(serviceGroupMaps,socketMsg,key,session);
            }
            return;
        }

        setMap(serviceKeySessionMap,socketMsg,key,session);
    }

    private static void setMap(Map<String,Map<String,Session>> map,SocketMsg socketMsg,String key,Session session)
    {
        Map<String, Session> keySessionMap = map.get(socketMsg.getServiceName());
            if (null == keySessionMap)
            {
                keySessionMap = new ConcurrentHashMap<>();
                keySessionMap.put(key,session);
                map.put(socketMsg.getServiceName(),keySessionMap);
            }
            else
                keySessionMap.put(key,session);
    }

    //根据服务获取组
    public static Map<String, Map<String, Session>> getGroupByService(String serviceName){
        return serviceGroupMap.get(serviceName);
    }
    //根据服务名获取绑定key
    public static Map<String, Session> getSessionByService(String serviceName){
        return serviceKeySessionMap.get(serviceName);
    }

    public synchronized static void remove(String serviceName,String group,String key){
        serviceGroupMap.get(serviceName).get(group).remove(key);

    }

    public synchronized static void remove(String serviceName,String key){
        serviceGroupMap.get(serviceName).remove(key);
    }

}
