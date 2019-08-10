package com.example.demo.small_talk.controller;

import com.allqj.ajf.thrift_test.SocketRequest;
import com.example.demo.service.FindChatService;
import com.example.demo.service.SendService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

    @GetMapping("/websocket11/{name}")
    public String webSocket1( @PathVariable String name,Model model){
        try{
            log.info("跳转到websocket的页面上");
            model.addAttribute("username",name);
            //sendService.send("a","a");
            return "index2";
        }
        catch (Exception e){
            log.info("跳转到websocket的页面上发生异常，异常信息是："+e.getMessage());
            return "error";
        }
    }

    @ResponseBody
    @PostMapping("/websocket")
    public void webSocket(HttpServletRequest reques, HttpSession session, @RequestBody SocketRequest request){
       try {
           String json = new Gson().toJson(session);
           System.out.println(json);

       }catch (Exception e){
            e.printStackTrace();
       }

        System.out.println(request.toString());
    }

    @ResponseBody
    @GetMapping("/websocket11")
    public void webSocket(@RequestBody String message){
        findChatService.findChat(message);
    }


}