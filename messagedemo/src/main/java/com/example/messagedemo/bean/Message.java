package com.example.messagedemo.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class Message implements Serializable {

    private String phone;

    private String userId;

    private String message;

    private String date;

    public Message(){}

    public Message(String phone, String userId, String message, String date) {
        this.phone = phone;
        this.userId = userId;
        this.message = message;
        this.date = date;
    }
}
