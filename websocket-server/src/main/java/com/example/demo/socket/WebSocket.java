package com.example.demo.socket;

import com.alibaba.fastjson.JSON;
import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.ContactDto;
import com.example.demo.Bean.DO.*;
import com.example.demo.Bean.SmallTalkMessageDto;
import com.example.demo.cache.SmallTalkClients;
import com.example.demo.common.WebApplicationContext;
import com.example.demo.notification.dfa.SensitiveWordFilter;
import com.example.demo.notification.dfa.SensitiveWordFilterUtil;
import com.example.demo.service.ServiceFacade;
import com.example.demo.util.NumUtil;
import com.example.demo.util.TempUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private Set<String> set = new HashSet<>();

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
        SmallTalkRecordDO recordDO = ServiceFacade.querySmallTalkRecord(originatorId);

        log.info("现在来连接的客户id："+session.getId()+"用户名："+originatorId);
        log.info("有新连接加入！ 当前在线人数" + SmallTalkClients.getOnlineNumber());
        try {
            //把自己的信息加入到map当中去
            SmallTalkClients.setClients(originatorId, this);

            //没有联系列表创建
            if(null==recordDO){
                TempUtils.getTemple().save(new SmallTalkRecordDO(originatorId,NumUtil.getTime(),new ArrayList<>()));
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
            send3(recordDO);

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
    private void send3(SmallTalkRecordDO recordDO){

        List<ClientReqParam> clientReqParamList = new ArrayList<>();
        //获取所有联系人id
        for(SmallTalkRecord smallTalkRecord : recordDO.getRecipientIds()){
            //查询单个联系人超过最后一次聊天时间的聊天记录
            SmallTalkMessageDO messageDO = TempUtils.getTemple().findOne(
                    Query.query(Criteria.where("codeId").is(NumUtil.countCode(smallTalkRecord.getRecipientId().hashCode(),originatorId.hashCode())))
                    ,SmallTalkMessageDO.class);
//            .and("message.$.timestamp").gt(smallTalkRecord.getTimestamp())
            //判空
            if(messageDO!=null){
                List<TalkMessage> message = messageDO.getMessage();
                message.stream().filter(bean->bean.getTimestamp() >= recordDO.getCreateTime()).forEach(bean->{
                    ClientReqParam clientReqParam = new ClientReqParam();
                    clientReqParam.setMessageType(4);
                    clientReqParam.setOriginatorId(bean.getRecipientId());
                    clientReqParam.setMessage(bean.getMessage());
                    clientReqParam.setSendTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(bean.getTimestamp()));
                    replaceSensitiveWord(clientReqParam);
                    clientReqParamList.add(clientReqParam);
                });
            }
        }
        //发送消息
        sendMessageTo(JSON.toJSONString(clientReqParamList),originatorId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        log.info("服务端发生了错误"+error.getMessage());
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose()
    {
        //删除在线人数
        SmallTalkClients.delNum();
        try {
            //删除链接
            SmallTalkClients.removeClients(originatorId);
            TempUtils.getTemple().upsert(
                    Query.query(Criteria.where("originatorId").is(originatorId)),
                    new Update().set("createTime",NumUtil.getTime()),
                    SmallTalkRecordDO.class);
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

            //消息过滤
            replaceSensitiveWord(clientReqParam);

            clientReqParam.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            //发送给自己
            sendMessageToMe(clientReqParam);

            //发送信息
            sendMessageTo(clientReqParam);
        }
        catch (Exception e){
            log.error("发生了错误了");
            e.printStackTrace();
        }
    }

    private void sendMessageToMe(ClientReqParam clientReqParam) {
        replaceSensitiveWord(clientReqParam);
        sendMessageTo(JSON.toJSONString(clientReqParam),originatorId);
    }

    /**
     * 敏感词过滤
     */
    private void replaceSensitiveWord(ClientReqParam clientReqParam){
        String message = clientReqParam.getMessage();
        if (StringUtils.isEmpty(message))return;
        clientReqParam.setMessage(SensitiveWordFilterUtil.filter(message));
    }

    /**
     * 保存联系列表及最新联系时间
     */
    private void saveRelationshipRecord(ClientReqParam clientReqParam){
        WebApplicationContext.sendEvent(new ContactDto(set,clientReqParam,contact));
    }

    /**
     * 保存消息记录
     */
    private void saveChatRecords(ClientReqParam clientReqParam){
        WebApplicationContext.sendEvent(new SmallTalkMessageDto(set,clientReqParam,contact));
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
     * @param clientReqParam 目标
     */
    private void sendMessageTo(ClientReqParam clientReqParam) {
        replaceSensitiveWord(clientReqParam);
        sendMessageTo(JSON.toJSONString(clientReqParam),clientReqParam.getRecipientId());
    }

    /**
     * 发送消息
     * @param message 消息
     * @param toUserName 发送目标
     */
    private void sendMessageTo(String message, String toUserName) {

        WebSocket webSocket = SmallTalkClients.getWebSocket(toUserName);
        if (null == webSocket) return;
        try {
            webSocket.session.getBasicRemote().sendText(message);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
            SmallTalkClients.removeClients(toUserName);
        }
    }

    public Session getSession(){
        return session;
    }

    public static synchronized Integer getOnlineCount() {
        return SmallTalkClients.getOnlineNumber();
    }

}