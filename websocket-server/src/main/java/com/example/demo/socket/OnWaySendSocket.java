package com.example.demo.socket;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.Bean.RequestParameter;
import com.example.demo.cache.OnWaySendCache;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

//只需要将路由和服务端发送的绑定 然后session绑定
@Slf4j
//@Component
//@ServerEndpoint("/websocket/{username}")
public class OnWaySendSocket {

    /**
     * 会话
     */
    private Session session;

    /**
     * 用户名称
     */
    private String codeId;

    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("codeId") String codeId, Session session)
    {
        log.info("现在来连接的客户id："+session.getId()+"用户名："+codeId);
        this.codeId = codeId;
        this.session = session;
        try {
            OnWaySendCache.addClientsConn(codeId, session);
        }
        catch (Exception e){
            log.info(codeId+"上线的时候通知所有人发生了错误");
        }



    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("服务端发生了错误"+error.getMessage());
    }
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose()
    {
        try {
            OnWaySendCache.removeConn(codeId,session);
        }
        catch (Exception e){
            log.info(codeId+"下线的时候通知所有人发生了错误");
        }
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session)
    {

    }

    public static void sendMessage(RequestParameter message) {
        try {
            sendMessage(JSONObject.toJSONString(message),message.getCodeId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message,String codeId) throws IOException {
        List<Session> clients = OnWaySendCache.getClientsConn(codeId);
        for (Session item : clients) {
            item.getAsyncRemote().sendText(message);
        }
    }

    public static Integer getOnlineCount(String codeId) {
        return OnWaySendCache.getClientsConn(codeId).size();
    }

}
