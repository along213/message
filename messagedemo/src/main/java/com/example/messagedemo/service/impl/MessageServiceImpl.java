package com.example.messagedemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.messagedemo.bean.Message;
import com.example.messagedemo.service.IMessageService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl implements IMessageService {

    public Message sendMessage(HttpServletRequest request){
        Message message = null;
        StringBuilder sb = null;
        try {
            BufferedReader reader = request.getReader();
            String line;
            sb = new StringBuilder();
            while ((line = reader.readLine())!=null){
                sb.append(line);
            }
            message = JSONObject.parseObject(sb.toString(),Message.class);

        }catch (IOException e){
            e.printStackTrace();
        }
        if (null==message){
            return null;
        }

        try {
            File file = new File("D:\\"+message.getPhone()+".txt");
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file, true)));
                out.write(sb.toString());
                out.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out!=null){
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public List<Message> getMessage(String phone) {
        FileInputStream fs = null;
        InputStreamReader is = null;
        BufferedReader br = null;
        List<Message> msg = new ArrayList<>();
        try {
            fs = new FileInputStream("D:\\"+phone+".txt");
            is = new InputStreamReader(fs);
            br = new BufferedReader(is);
            String line;
            while ((line = br.readLine())!=null){
                msg.add(JSONObject.parseObject(line,Message.class));
            }
            return msg;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return msg;
    }


}
