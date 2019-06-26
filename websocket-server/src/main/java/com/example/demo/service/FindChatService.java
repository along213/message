package com.example.demo.service;

import com.example.demo.Bean.DO.SmallTalkRecordDO;

import java.util.List;

public interface FindChatService {

    SmallTalkRecordDO findChat(String message);

}
