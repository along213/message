package com.example.demo.cache;

import com.example.demo.socket.WebSocket;

import javax.websocket.Session;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OnWaySendCache {

    /**
     * 单向发送
     */
    private static Map<String, CopyOnWriteArrayList<Session>> clientsConn = new ConcurrentHashMap<>();

    //private static ConcurrentHashMap<String,Integer> readNum = new ConcurrentHashMap<>();
    /**
     * 添加连接
     * @param codeId 唯一标识
     * @param session session
     */
    public synchronized static void addClientsConn(String codeId,Session session){
        CopyOnWriteArrayList<Session> sessionList = clientsConn.get(codeId);
        if (null==sessionList){
            CopyOnWriteArrayList<Session> list = new CopyOnWriteArrayList<>();
            list.add(session);
            clientsConn.put(codeId,list);
            return;
        }
        clientsConn.get(codeId).add(session);
    }

    /**
     * 获取连接
     * @param codeId 唯一标识
     */
    public static List<Session> getClientsConn(String codeId){
        return clientsConn.get(codeId);
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
