package com.example.demo.service;

import com.example.demo.Bean.DO.DocumentMsg;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import com.example.demo.Repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FindChatServiceImpl implements FindChatService{

    @Autowired
    private ChatRepository chatRepository;

    @Override
    public SmallTalkRecordDO findChat(String message) {
        Optional<List<DocumentMsg>> byMsg = chatRepository.findByMsg(message);
        List<DocumentMsg> documentMsg = null;
        if (byMsg.isPresent()) {
            documentMsg = byMsg.get();
            System.out.println(documentMsg.toString());
        }
        //获取相关id

        return null;
    }
}
