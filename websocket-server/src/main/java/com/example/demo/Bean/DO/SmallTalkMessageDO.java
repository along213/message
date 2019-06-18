package com.example.demo.Bean.DO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SmallTalk_Msg")
public class SmallTalkMessageDO implements Serializable {

    private String codeId;

    private List<TalkMessage> message;



}
