package com.example.demo.notification.service;

import com.allqj.ajf.thrift_test.ISendMessageService;
import com.allqj.ajf.thrift_test.SocketRequest;
import com.allqj.ajf.thrift_test.SocketResponse;
import com.allqj.qjf.thrift_service.spring.annotation.ThriftService;
import com.example.demo.cache.ServiceGroupKeyMap;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.util.Map;

@ThriftService(serviceType = ISendMessageService.class,serviceProcessor = ISendMessageService.Processor.class)
public class ISendMessageServiceImpl extends SendMsgService implements ISendMessageService.Iface {

    @Override
    public SocketResponse send(SocketRequest request) {
        System.out.println(request.toString());
        //final List<String> list = new ArrayList<>();
        //判断是否有组
        try {
        if (!StringUtils.isEmpty(request.getGroup())) {
            Map<String, Map<String, Session>> groupByService = ServiceGroupKeyMap.getGroupByService(request.getProjectId());
            if (null == groupByService) return result(false,"未找到");
            if (sendOne(groupByService.get(request.getGroup()),request))return result(true,null);
            sendMessage(groupByService.get(request.getGroup()),request.getMessage());
        }else {
            if (sendOne(ServiceGroupKeyMap.getSessionByService(request.getProjectId()),request))return result(true,null);
            sendMessage(ServiceGroupKeyMap.getSessionByService(request.getProjectId()),request.getMessage());
        }
        }catch (RuntimeException e){
            return result(false,e.getMessage());
        }
        //判断是否开启失败回调
        //将key返回
        return result(true,null);
    }

    /**
     * 单独发送
     * @param sessionMap sessionMap
     * @param request request
     * @return boolean
     */
    private boolean sendOne(Map<String, Session> sessionMap,SocketRequest request) {
        if (!StringUtils.isEmpty(request.getKey())){
            try {
                sessionMap.get(request.getKey()).getBasicRemote().sendText(request.getMessage());
                return true;
            }catch (NullPointerException e){
                e.printStackTrace();
                throw new RuntimeException("未找到目標");
            }catch (Exception e){
                e.printStackTrace();
                ServiceGroupKeyMap.remove(request.getProjectId(),request.getGroup());
                throw new RuntimeException("发送消息错误");
            }
        }
        return false;
    }

    private SocketResponse result(boolean b,String message){
        SocketResponse response = new SocketResponse();
        response.setResultType(b);
        response.setMessage(message);
        return response;
    }
}
