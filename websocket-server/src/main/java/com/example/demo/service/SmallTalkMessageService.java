package com.example.demo.service;

import com.example.demo.Bean.ClientReqParam;
import com.example.demo.Bean.DO.SmallTalkMessageDO;
import com.example.demo.Bean.SmallTalkMessageDto;

public interface SmallTalkMessageService {

    void save(SmallTalkMessageDto messageDto);

    SmallTalkMessageDO query();

}
