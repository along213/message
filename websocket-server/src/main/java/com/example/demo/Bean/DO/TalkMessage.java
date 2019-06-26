package com.example.demo.Bean.DO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TalkMessage implements Serializable {

    private String id;

    private String originatorId;

    private String recipientId;

    private String message;

    /**
     * 发送时间
     */
    private Long timestamp;

}
