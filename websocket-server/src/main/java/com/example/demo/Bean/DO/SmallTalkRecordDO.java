package com.example.demo.Bean.DO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SmallTalk_record")
public class SmallTalkRecordDO implements Serializable {

    /**
     * 唯一标识
     */
    private String originatorId;

    private Date createTime;

    private List<SmallTalkRecord> recipientIds;
}
