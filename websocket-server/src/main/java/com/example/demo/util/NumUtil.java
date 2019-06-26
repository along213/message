package com.example.demo.util;

import java.util.Date;

public class NumUtil {

    public static String countCode(long a,long b){
        return a<b?a+"&&"+b:b+"&&"+a;
    }

    public static Long getTime(){
        return new Date().getTime();
    };

}
