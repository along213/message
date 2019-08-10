package com.example.demo.socket;

import com.alibaba.fastjson.JSON;
import com.example.demo.Bean.SocketMsg;
import com.example.demo.cache.ServiceGroupKeyMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

//只需要将路由和服务端发送的绑定 然后session绑定
@Slf4j
@Component
@ServerEndpoint("/webSocketPush/{token}")
public class OnWaySendSocket {

    /**
     * 用户名称
     */
    private String key;

    private SocketMsg socketMsg;

    private int index = 0;

    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session)
    {
        this.key = token;
        System.out.println(key);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("服务端发生了错误"+error.getMessage());
        delete();
    }
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        delete();
    }


    private void delete(){
        try
        {
            if (StringUtils.isEmpty(socketMsg.getGroup())){
                ServiceGroupKeyMap.remove(socketMsg.getServiceName(),socketMsg.getGroup(),key);
                return;
            }
            ServiceGroupKeyMap.remove(socketMsg.getServiceName(),key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.info(key+"下线的时候通知所有人发生了错误");
        }
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session){
        index++;
        if (index>1)  return;
        System.out.println(message);
        socketMsg = JSON.parseObject(message,SocketMsg.class);
        ServiceGroupKeyMap.save(socketMsg,key,session);
    }

}
