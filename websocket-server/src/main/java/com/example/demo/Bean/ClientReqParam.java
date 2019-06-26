package com.example.demo.Bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClientReqParam {

    private String originatorId;

    private String recipientId;

    private String message;

    private Integer messageType;

    private Set<String> onlineUsers;

    private String sendTime;
}
