package com.example.demo.notification.dfa;

import com.example.demo.Bean.ClientReqParam;
import org.springframework.stereotype.Service;

@Service
public class DefStrategyServiceImpl implements SensitiveWordStrategyService{

    @Override
    public boolean filter(ClientReqParam clientReqParam) {
        return true;
    }
}
