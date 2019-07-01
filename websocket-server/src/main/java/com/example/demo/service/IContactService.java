package com.example.demo.service;

import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.ContactDto;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import com.example.demo.socket.WebSocket;

import java.util.Map;
import java.util.Set;

public interface IContactService {

    void save(ContactDto contact);

    SmallTalkRecordDO query(String uuId);

}
