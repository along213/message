package com.example.messagedemo.controller;

import com.example.messagedemo.bean.Message;
import com.example.messagedemo.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private IMessageService messageService;

    @PutMapping("/message")
    public Message sendMessage(HttpServletRequest request){
        return messageService.sendMessage(request);
    }

    @GetMapping("/message/{phone}")
    public List<Message> getMessage(@PathVariable String phone){
        return messageService.getMessage(phone);
    }

}
