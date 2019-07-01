package com.example.demo.service;

import com.example.demo.Bean.ContactDto;
import com.example.demo.Bean.DO.SmallTalkRecordDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceFacade {

    private static IContactService contactService;

    @Autowired
    public ServiceFacade(IContactService contactService){
        this.contactService = contactService;
    }

    public static void save(ContactDto contact){
        contactService.save(contact);
    }

    public static SmallTalkRecordDO querySmallTalkRecord(String id){
        return contactService.query(id);
    }

}
