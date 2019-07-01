package com.example.demo.service.impl;

import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.DO.DocumentMsg;
import com.example.demo.Bean.DO.SmallTalkMessageDO;
import com.example.demo.Bean.DO.TalkMessage;
import com.example.demo.Bean.SmallTalkMessageDto;
import com.example.demo.service.SmallTalkMessageService;
import com.example.demo.socket.WebSocket;
import com.example.demo.util.EsUtil;
import com.example.demo.util.NumUtil;
import com.example.demo.util.TempUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmallTalkMessageServiceImpl implements SmallTalkMessageService {

    @EventListener
    @Override
    public void save(SmallTalkMessageDto messageDto) {

        ClientReqParam clientReqParam = messageDto.getClientReqParam();
        Map<String, WebSocket> contact = messageDto.getContact();
        String codeId = NumUtil.countCode(clientReqParam.getRecipientId().hashCode(), clientReqParam.getOriginatorId().hashCode());

        //生成唯一id
        String uuid = UUID.randomUUID().toString().replaceAll("-","");

        //记录存在
        if (contact.containsKey(clientReqParam.getRecipientId()))
            TempUtils.getTemple().upsert(Query.query(Criteria.where("codeId").is(codeId)),
                    new Update().addToSet("message",new TalkMessage(uuid,clientReqParam.getOriginatorId(),
                            clientReqParam.getRecipientId(),
                            clientReqParam.getMessage(),
                            NumUtil.getTime())),SmallTalkMessageDO.class);
        else {
            //不存在
            List<TalkMessage> messageArrayList = new ArrayList<>();
            messageArrayList.add(new TalkMessage(uuid,clientReqParam.getOriginatorId(),
                    clientReqParam.getRecipientId(),
                    clientReqParam.getMessage(),
                    NumUtil.getTime()));
            TempUtils.getTemple().save(new SmallTalkMessageDO(codeId,messageArrayList));
        }
        //保存到es中
        saveChatRecordsToEs(clientReqParam,uuid);
    }

    @Override
    public SmallTalkMessageDO query() {
        return null;
    }

    /**
     * 将聊天信息保存到es中
     * @param clientReqParam es
     * @param uuid 唯一id
     */
    private void saveChatRecordsToEs(ClientReqParam clientReqParam, String uuid) {
        EsUtil.getChatRepository().save(new DocumentMsg(uuid,clientReqParam.getMessage()));
    }

}
