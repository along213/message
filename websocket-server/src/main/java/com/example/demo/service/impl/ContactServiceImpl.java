package com.example.demo.service.impl;

import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.ContactDto;
import com.example.demo.Bean.DO.SmallTalkRecord;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import com.example.demo.service.IContactService;
import com.example.demo.socket.WebSocket;
import com.example.demo.util.NumUtil;
import com.example.demo.util.TempUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class ContactServiceImpl implements IContactService {

    @EventListener
    @Override
    public void save(ContactDto contactDto) {
        ClientReqParam clientReqParam = contactDto.getClientReqParam();
        Set<String> set = contactDto.getSet();
        Map<String, WebSocket> contact = contactDto.getContact();
        saveRelationshipRecord(clientReqParam.getOriginatorId(),clientReqParam.getRecipientId(),contact);
        //判断联系列表中是否存在该联系人
        if (set.contains(clientReqParam.getRecipientId()))return;
        //查询是否存在
        SmallTalkRecordDO smallTalkRecordDO = querySmallTalkRecord(clientReqParam.getRecipientId());

        if (null == smallTalkRecordDO) return;
        //判断联系列表是否存在消息接收方
        for (SmallTalkRecord bean : smallTalkRecordDO.getRecipientIds())
            if(clientReqParam.getOriginatorId().equals(bean.getRecipientId())){
                set.add(clientReqParam.getOriginatorId());
                return;
            }

        set.add(clientReqParam.getRecipientId());

        saveRelationshipRecord(clientReqParam.getRecipientId(),clientReqParam.getOriginatorId(),contact);
    }

    /**
     * 保存联系列表及最新联系时间
     */
    private void saveRelationshipRecord(String originatorId,String recipientId,Map<String, WebSocket> contact){
        //不存在新增
        if (contact.containsKey(recipientId))
            TempUtils.getTemple().upsert(Query.query(Criteria.where("originatorId").is(originatorId).and("recipientIds.recipientId").is(recipientId)),
                    new Update().set("recipientIds.$.timestamp", NumUtil.getTime()),
                    SmallTalkRecordDO.class);
        else
            TempUtils.getTemple().upsert(Query.query(Criteria.where("originatorId").is(originatorId)),
                    new Update().addToSet("recipientIds",new SmallTalkRecord(0,recipientId,NumUtil.getTime())),
                    SmallTalkRecordDO.class);
    }

    //查询联系记录
    private SmallTalkRecordDO querySmallTalkRecord(String originatorId){
        return TempUtils.getTemple().
                findOne(Query.query(Criteria.where("originatorId").is(originatorId)), SmallTalkRecordDO.class);
    }

    @Override
    public SmallTalkRecordDO query(String originatorId) {
        return querySmallTalkRecord(originatorId);
    }
}
