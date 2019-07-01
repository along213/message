package com.example.demo.dfa;

import com.example.demo.Bean.ClientReqParam;

public interface SensitiveWordStrategyService {

    boolean filter(ClientReqParam clientReqParam);

}
