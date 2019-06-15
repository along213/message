package com.example.demo.Bean.DO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SmallTalk_Msg")
public class SmallTalkMessageDO implements Serializable {

    private Integer readId;

    private String codeId;

    private String originatorId;

    private String recipientId;

    private Date timestamp;

    private String message;

}
