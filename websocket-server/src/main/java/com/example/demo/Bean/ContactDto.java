package com.example.demo.Bean;

import com.example.demo.socket.WebSocket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto {

    Set<String> set;
    ClientReqParam clientReqParam;
    Map<String, WebSocket> contact;

}
