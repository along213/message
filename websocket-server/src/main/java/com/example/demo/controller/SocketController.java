package com.example.demo.controller;

import com.example.demo.Bean.RequestParameter;
import com.example.demo.service.FindChatService;
import com.example.demo.service.SendService;
import com.example.demo.socket.OnWaySendSocket;
import com.example.demo.socket.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Controller
public class SocketController {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private FindChatService findChatService;

    //@Resource
    private SendService sendService;

    @GetMapping("/websocket/{id}/{name}")
    public String webSocket(@PathVariable String name, Model model){
        try{
            log.info("跳转到websocket的页面上");
            model.addAttribute("username",name);
            //sendService.send("a","a");
            return "index";
        }
        catch (Exception e){
            log.info("跳转到websocket的页面上发生异常，异常信息是："+e.getMessage());
            return "error";
        }
    }

    @ResponseBody
    @PostMapping("/websocket")
    public void webSocket(@RequestBody RequestParameter message){
        OnWaySendSocket.sendMessage(message);
        System.out.println(message.toString());
    }

    @ResponseBody
    @PostMapping("/websocket11")
    public void webSocket(@RequestBody String message){
        findChatService.findChat(message);
    }


}