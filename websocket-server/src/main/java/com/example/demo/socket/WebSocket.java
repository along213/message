package com.example.demo.socket;

import com.alibaba.fastjson.JSON;
import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.DO.RecordPersonMsg;
import com.example.demo.Bean.DO.SmallTalkMessageDO;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import com.example.demo.Bean.RequestParameter;
import com.example.demo.cache.OnWaySendCache;
import com.example.demo.cache.SmallTalkClients;
import com.example.demo.util.NumUtil;
import com.example.demo.util.TempUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/websocket/{originatorId}")
public class WebSocket {

    /**
     * 会话
     */
    private Session session;

    /**
     * 用户名称
     */
    private String originatorId;

    /**
     * 消息记录数
     */
    private int id = 1;

    /**
     * 所有联系列表
     */
    private  Map<String, WebSocket> contact = new ConcurrentHashMap<>();

    /**
     * 负责 查询用户联系列表 通知在线人数 查询未读消息
     * messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
     * 建立连接
     * @param session session
     */
    @OnOpen
    public void onOpen(@PathParam("originatorId") String originatorId, Session session)
    {
        SmallTalkClients.addNum();
//        List<RecordPersonMsg> l = new ArrayList<>();
//        l.add(new RecordPersonMsg("13473500739",1));
//        TempUtils.getTemple().save(new SmallTalkRecordDO("13132191219",new Date(),l));


        Query query = new Query();
        Criteria criteria = Criteria.where("originatorId").is(originatorId);
        query.addCriteria(criteria);
        SmallTalkRecordDO recordDO = TempUtils.getTemple().findOne(query, SmallTalkRecordDO.class);

//        Query query1 = new Query();
//        Criteria criteria1 = Criteria.where("originatorId").is("13132191219").and("recipientIds.id").is("13473500739");
//        Update update = Update.update("recipientIds.$.readId",2);
//        query1.addCriteria(criteria1);
//        TempUtils.getTemple().upsert(query1,update,SmallTalkRecordDO.class);


        log.info("现在来连接的客户id："+session.getId()+"用户名："+originatorId);
        this.originatorId = originatorId;
        this.session = session;
        log.info("有新连接加入！ 当前在线人数" + SmallTalkClients.getOnlineNumber());
        try {
            //把自己的信息加入到map当中去
            SmallTalkClients.setClients(originatorId, this);

            if(null==recordDO)return;

            //获取所有联系列表
            recordDO.getRecipientIds().forEach(bean->{
                contact.put(bean.getId(), SmallTalkClients.getWebSocket(bean.getId()));
            });

            //TODO 先给所有联系人发送通知，说我上线了
            send1();
            //TODO 给自己发一条消息：告诉自己现在都有谁在线
            send2();
            //TODO 发送未读消息
            send3();
        }
        catch (IOException e){
            log.info(originatorId+"上线的时候通知所有人发生了错误");
        }
    }

    //在线通知
    private void send1(){
        ClientReqParam clientReqParam = new ClientReqParam();
        clientReqParam.setMessageType(1);
        clientReqParam.setOriginatorId(originatorId);
        sendMessageAll(JSON.toJSONString(clientReqParam));
    }

    //在线名单
    private void send2() throws IOException{
        ClientReqParam clientReq = new ClientReqParam();
        clientReq.setMessageType(3);
        //在线名单
        clientReq.setOnlineUsers(contact.keySet());
        sendMessageTo(JSON.toJSONString(clientReq),originatorId);
    }

    //未读消息
    private void send3(){
//        contact.forEach((key,value)->{
//            if (value == null)
//                value = SmallTalkClients.getWebSocket(key);
//            if (value!=null){
//                ClientReqParam clientReqParam = new ClientReqParam();
//                clientReqParam.setMessageType(1);
//                clientReqParam.setOriginatorId(key);
//                clientReqParam.setMessage();
//                sendMessageTo(JSON.toJSONString(clientReqParam),originatorId);
//            }
//        });
        List<SmallTalkMessageDO> messageDOList = TempUtils.getTemple().find(Query.query(Criteria.where("codeId").is(originatorId)), SmallTalkMessageDO.class);
        messageDOList.forEach(bean->{
            ClientReqParam clientReqParam = new ClientReqParam();
            clientReqParam.setMessageType(4);
            clientReqParam.setOriginatorId(bean.getRecipientId());
            clientReqParam.setMessage(bean.getMessage());
            clientReqParam.setSendTime(bean.getTimestamp());
            sendMessageTo(JSON.toJSONString(clientReqParam),originatorId);
        });
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
        SmallTalkClients.delNum();
        SmallTalkClients.removeClients(originatorId);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String,Object> map1 = Maps.newHashMap();
            map1.put("messageType",2);
            map1.put("onlineUsers",contact.keySet());
            map1.put("originatorId",originatorId);
        }
        catch (Exception e){
            log.info(originatorId+"下线的时候通知所有人发生了错误");
        }
        log.info("有连接关闭！ 当前在线人数" + SmallTalkClients.getOnlineNumber());
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
        //TODO 保存联系人 保存联系记录
        try {
            log.info("来自客户端消息：" + message+"客户端的id是："+session.getId());
            ClientReqParam clientReqParam = JSON.parseObject(message, ClientReqParam.class);
            String countCode = NumUtil.countCode(originatorId.hashCode(), clientReqParam.getRecipientId().hashCode());
            //保存阅读id
            saveReadId(countCode);
            //保存联系记录
            //TempUtils.getTemple().save(new SmallTalkMessageDO(countCode,id++,originatorId,clientReqParam.getRecipientId(),new Date()));
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            clientReqParam.setMessageType(4);
            sendMessageTo(JSON.toJSONString(clientReqParam),clientReqParam.getRecipientId(),countCode);
        }
        catch (Exception e){
            log.error("发生了错误了");
            e.printStackTrace();
        }
    }

    private void saveReadId(String countCode){
        Integer readId = OnWaySendCache.getReadId(countCode);
        if (null == readId) {
            OnWaySendCache.putReadId(countCode, id);
        } else {
            OnWaySendCache.putReadId(countCode, ++readId);
        }
    }

    private void sendMessageAll(String message){
        contact.values().forEach(socket->{
            try {
                socket.session.getBasicRemote().sendText(message);
            }catch (Exception e){
                log.info("发生了错误了");
                try {
                    socket.session.close();
                }catch (IOException ioe){

                }
            }
        });
    }

    private void sendMessageTo(String message, String toUserName,String countCode) {
        WebSocket webSocket = SmallTalkClients.getWebSocket(toUserName);
        if (null == webSocket){
            return;
        }
        try {
            webSocket.session.getBasicRemote().sendText(message);
        }catch (Exception e){
            log.error(e.getMessage());
            OnWaySendCache.putReadId(countCode);
            SmallTalkClients.removeClients(toUserName);
        }
    }

    /**
     * 发送在线或离线消息
     * @param message 消息
     * @param toUserName 发送目标
     * @throws IOException io异常
     */
    private void sendMessageTo(String message, String toUserName) throws IOException {
        WebSocket webSocket = SmallTalkClients.getWebSocket(toUserName);
        webSocket.session.getBasicRemote().sendText(message);
    }

    public static void sendMessage(RequestParameter message){

    }

    public Session getSession(){
        return session;
    }

    public static synchronized Integer getOnlineCount() {
        return SmallTalkClients.getOnlineNumber();
    }

}