package com.example.demo.Bean.DO;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "socket_msg", type = "document")
public class DocumentMsg {
    @Id
    private String id;

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String msg;



}



