package com.example.demo.notification.service;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendMsgService{

    //发送消息
    protected void sendMessage(Map<String, Session> stringSessionMap, List<String> list, String message){
        stringSessionMap.forEach((key,value)->
        {
            try
            {
                value.getBasicRemote().sendText(message);
            }catch (Exception e)
            {
                e.printStackTrace();
                list.add(key);
            }
        });
    }

    //发送消息
    protected void sendMessage(Map<String, Session> stringSessionMap, String message){
        List<String> list = new ArrayList<>();
        stringSessionMap.forEach((key,value)->
        {
            try
            {
                value.getBasicRemote().sendText(message);
            }catch (Exception e)
            {
                e.printStackTrace();
                list.add(key);
            }
        });
        list.forEach(stringSessionMap::remove);
    }

}
