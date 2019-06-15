package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TempUtils {

    /**
     * mongodb
     */
    private static MongoTemplate mongoTemplate;

    /**
     * 使用构造注入
     * @param mongoTemplate mongodb
     */
    @Autowired
    public TempUtils(MongoTemplate mongoTemplate){
        TempUtils.mongoTemplate = mongoTemplate;
    }

    public static MongoTemplate getTemple(){
        return mongoTemplate;
    }

//    public static<T> T queryMsg(Query query, Class<T> entityClass){
//        T t = (T) mongoTemplate.find(query, entityClass);
//        return t;
//    }

}
