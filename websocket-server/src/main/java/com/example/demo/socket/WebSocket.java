package com.example.demo.socket;

import com.alibaba.fastjson.JSON;
import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.DO.SmallTalkMessageDO;
import com.example.demo.Bean.DO.SmallTalkRecord;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import com.example.demo.Bean.DO.TalkMessage;
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

    //TODO 连接超时？ 对接权限？

    /**
     * 会话
     */
    private Session session;

    /**
     * 用户名称
     */
    private String originatorId;

    /**
     * 所有联系列表
     */
    private  Map<String, WebSocket> contact = new HashMap<>();

    /**
     * 负责 查询用户联系列表 通知在线人数 查询未读消息
     * messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
     * 建立连接
     * @param session session
     */
    @OnOpen
    public void onOpen(@PathParam("originatorId") String originatorId, Session session)
    {
        this.originatorId = originatorId;
        this.session = session;
        SmallTalkClients.addNum();
        //查询联系记录
        SmallTalkRecordDO recordDO = TempUtils.getTemple().
                findOne(Query.query(Criteria.where("originatorId").is(originatorId)), SmallTalkRecordDO.class);

        log.info("现在来连接的客户id："+session.getId()+"用户名："+originatorId);
        log.info("有新连接加入！ 当前在线人数" + SmallTalkClients.getOnlineNumber());
        try {
            //把自己的信息加入到map当中去
            SmallTalkClients.setClients(originatorId, this);
            //没有联系记录创建
            if(null==recordDO){
                TempUtils.getTemple().save(new SmallTalkRecordDO(originatorId,new Date(),new ArrayList<>()));
                return;
            }
            //获取所有联系列表
            recordDO.getRecipientIds().forEach(bean->{
                contact.put(bean.getRecipientId(), SmallTalkClients.getWebSocket(bean.getRecipientId()));
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

    //TODO 在线名单
    //在线名单
    private void send2() throws IOException{
        ClientReqParam clientReq = new ClientReqParam();
        clientReq.setMessageType(3);
        //在线名单
        Set<String> set = new HashSet<>();
        contact.forEach((key,value)->{
            if (null != value)
                set.add(key);
        });
        clientReq.setOnlineUsers(set);
        sendMessageTo(JSON.toJSONString(clientReq),originatorId);
    }

    //未读消息
    private void send3(){

        List<ClientReqParam> clientReqParamList = new ArrayList<>();

        //获取所有联系人id
        for(String key : contact.keySet()){

            //todo 结果可能不对
            //查询单个联系人的未发送聊天记录
            SmallTalkMessageDO messageDO = TempUtils.getTemple().findOne(
                    Query.query(Criteria.where("codeId").is(NumUtil.countCode(key.hashCode(),originatorId.hashCode()))
                    .and("message.$.type").is("1"))
                    ,SmallTalkMessageDO.class);
            //判空
            if(messageDO!=null){
                List<TalkMessage> message = messageDO.getMessage();

                message.forEach(bean->{
                    ClientReqParam clientReqParam = new ClientReqParam();
                    clientReqParam.setMessageType(4);
                    clientReqParam.setOriginatorId(bean.getRecipientId());
                    clientReqParam.setMessage(bean.getMessage());
                    clientReqParam.setSendTime(bean.getTimestamp());
                    clientReqParamList.add(clientReqParam);
                });
            }
        }
        //发送消息
        sendMessageTo(JSON.toJSONString(clientReqParamList),originatorId);
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
            ClientReqParam clientReqParam = new ClientReqParam();
            clientReqParam.setOriginatorId(originatorId);
            clientReqParam.setOnlineUsers(contact.keySet());
            clientReqParam.setMessageType(2);

        }
        catch (Exception e){
            log.info(originatorId+"下线的时候通知所有人发生了错误");
        }
        log.info("有连接关闭！ 当前在线人数" + SmallTalkClients.getOnlineNumber());
    }

    /**
     * 收到客户端的消息
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
            //保存联系记录
            saveRelationshipRecord(clientReqParam);
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            clientReqParam.setMessageType(4);
            //保存聊天记录
            saveChatRecords(clientReqParam);
            //联系列表
            contact.put(clientReqParam.getRecipientId(),SmallTalkClients.getWebSocket(clientReqParam.getRecipientId()));
            //发送信息
            sendMessageTo(JSON.toJSONString(clientReqParam),clientReqParam);
        }
        catch (Exception e){
            log.error("发生了错误了");
            e.printStackTrace();
        }
    }

    /**
     * 保存联系列表及最新联系时间
     */
    private void saveRelationshipRecord(ClientReqParam clientReqParam){
        //不存在新增
        if (contact.containsKey(clientReqParam.getRecipientId()))
            TempUtils.getTemple().upsert(Query.query(Criteria.where("originatorId").is(originatorId).and("recipientIds.recipientId").is(clientReqParam.getRecipientId())),
                new Update().set("recipientIds.$.timestamp",new Date()),
                SmallTalkRecordDO.class);
        else
            TempUtils.getTemple().upsert(Query.query(Criteria.where("originatorId").is(originatorId)),
                new Update().addToSet("recipientIds",new SmallTalkRecord(clientReqParam.getRecipientId(),new Date())),
                SmallTalkRecordDO.class);
    }

    /**
     * 保存消息记录
     */
    private void saveChatRecords(ClientReqParam clientReqParam){
        //todo
        String codeId = NumUtil.countCode(clientReqParam.getRecipientId().hashCode(), originatorId.hashCode());
        //记录存在
        if (contact.containsKey(clientReqParam.getRecipientId()))
            editChatRecords(clientReqParam,"0");
         else {
            //不存在
            List<TalkMessage> messageArrayList = new ArrayList<>();
            messageArrayList.add(new TalkMessage(clientReqParam.getOriginatorId(),
                    clientReqParam.getRecipientId(),
                    clientReqParam.getMessage(),
                    new Date(),"0"));
            TempUtils.getTemple().save(new SmallTalkMessageDO(codeId,messageArrayList));
        }
    }

    /**
     * 修改消息状态
     */
    private void editChatRecords(ClientReqParam clientReqParam,String type){
        //todo
        String codeId = NumUtil.countCode(clientReqParam.getRecipientId().hashCode(), originatorId.hashCode());
        TempUtils.getTemple().upsert(Query.query(Criteria.where("codeId").is(codeId)),
                new Update().addToSet("message",new TalkMessage(clientReqParam.getOriginatorId(),
                        clientReqParam.getRecipientId(),
                        clientReqParam.getMessage(),
                        new Date(),type)),SmallTalkMessageDO.class);
    }

    //批量发送
    private void sendMessageAll(String message){
        contact.forEach((key,socket)->{
            try {
                socket = socket == null ? SmallTalkClients.getWebSocket(key) : socket;
                if (socket != null){
                    socket.session.getBasicRemote().sendText(message);
                    contact.put(key,socket);
                }
            }catch (Exception e){
                log.info("发生了错误了");
                contact.remove(key);
            }
        });
    }

    /**
     * @param message 消息
     * @param clientReqParam 目标
     */
    private void sendMessageTo(String message, ClientReqParam clientReqParam) {
        WebSocket webSocket = SmallTalkClients.getWebSocket(clientReqParam.getRecipientId());
        if (null == webSocket) return;
        try {
            webSocket.session.getBasicRemote().sendText(message);
            editChatRecords(clientReqParam,"1");
        }catch (Exception e){
            log.error(e.getMessage());
            SmallTalkClients.removeClients(clientReqParam.getRecipientId());
        }
    }

    /**
     * 发送消息
     * @param message 消息
     * @param toUserName 发送目标
     */
    private void sendMessageTo(String message, String toUserName) {

        WebSocket webSocket = SmallTalkClients.getWebSocket(toUserName);
        try {
            webSocket.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 服务端推送数据->前端 TODO
     * @param message 请求参数
     */
    public static void sendMessage(RequestParameter message){

    }

    public Session getSession(){
        return session;
    }

    public static synchronized Integer getOnlineCount() {
        return SmallTalkClients.getOnlineNumber();
    }

}