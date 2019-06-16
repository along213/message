package com.example.demo.cache;

import com.example.demo.socket.WebSocket;

import javax.websocket.Session;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnWaySendCache {

    /**
     * 单向发送
     */
    private static Map<String, ConcurrentHashMap<Session,WebSocket>> clientsConn = new ConcurrentHashMap<>();

    //private static ConcurrentHashMap<String,Integer> readNum = new ConcurrentHashMap<>();
    /**
     * 添加连接
     * @param codeId 唯一标识
     * @param webSocket socket
     */
    public synchronized static void addClientsConn(String codeId,WebSocket webSocket){
        ConcurrentHashMap<Session,WebSocket> webSocketMaps = clientsConn.get(codeId);
        if (null==webSocketMaps){
            ConcurrentHashMap<Session,WebSocket> map = new ConcurrentHashMap<>();
            map.put(webSocket.getSession(),webSocket);
            clientsConn.put(codeId,map);
            return;
        }
        clientsConn.get(codeId).put(webSocket.getSession(),webSocket);
    }

    /**
     * 获取连接
     * @param codeId 唯一标识
     */
    public static Collection<WebSocket> getClientsConn(String codeId){
        return clientsConn.get(codeId).values();
    }

    /**
     * 删除
     * @param codeId 唯一标识
     * @param session session
     */
    public static void removeConn(String codeId,Session session){
        clientsConn.get(codeId).remove(session);
    }

//    /**
//     * 添加
//     * @param codeId 唯一标识
//     * @param readId 读取的id
//     */
//    public static void putReadId(String codeId,Integer readId){
//        readNum.put(codeId,readId);
//    }
//
//    public static Integer getReadId(String codeId){
//        return readNum.get(codeId);
//    }
//
//    public static void putReadId(String codeId){
//        Integer num = readNum.get(codeId);
//        readNum.put(codeId,--num);
//    }

}
