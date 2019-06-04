package com.example.messagedemo.service;

import com.example.messagedemo.bean.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IMessageService {

    Message sendMessage(HttpServletRequest request);

    List<Message> getMessage(String phone);

}
