package com.example.demo.Bean.DO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmallTalkRecord {

    private Integer readLine;

    private String recipientId;

    private Long timestamp;

}
