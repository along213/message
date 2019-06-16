package com.example.demo.util;

public class NumUtil {

    public static String countCode(long a,long b){
        return a<b?a+"&&"+b:b+"&&"+a;
    }

}
