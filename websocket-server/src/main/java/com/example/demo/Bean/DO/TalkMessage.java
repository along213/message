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

    private String originatorId;

    private String recipientId;

    private String message;

    private Date timestamp;

    private String type;



}
