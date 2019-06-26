package com.example.demo.util;

import com.example.demo.Repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EsUtil {

    private static ChatRepository chatRepository;

    @Autowired
    public EsUtil(ChatRepository chatRepository){
        EsUtil.chatRepository = chatRepository;
    }

    public static ChatRepository getChatRepository(){
        return chatRepository;
    }

}
