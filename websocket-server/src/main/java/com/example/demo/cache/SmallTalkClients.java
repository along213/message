package com.example.demo.cache;

import com.example.demo.socket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SmallTalkClients {

    /**
     * 在线人数
     */
    private static volatile AtomicInteger onlineNumber = new AtomicInteger(0);

    /**
     * 以用户的姓名为key，WebSocket为对象保存起来
     */
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<>();

    public static Integer getOnlineNumber() {
        return onlineNumber.get();
    }

    public static void addNum() {
        onlineNumber.addAndGet(1);
    }

    public static void delNum() {
        onlineNumber.addAndGet(-1);
    }

    public static WebSocket getWebSocket(String codeId){
        return clients.get(codeId);
    }

    public static void setClients(String codeId,WebSocket webSocket){
        clients.put(codeId,webSocket);
    }

    public static void removeClients(String codeId){
        clients.remove(codeId);
    }

}
