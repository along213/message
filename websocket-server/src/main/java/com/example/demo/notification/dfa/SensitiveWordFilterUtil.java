package com.example.demo.notification.dfa;

public class SensitiveWordFilterUtil {

    private static SensitiveWordFilter sensitiveWordFilter = new SensitiveWordFilter();

    public static String filter(String message){
        return sensitiveWordFilter.replaceSensitiveWord(message, SensitiveWordFilter.MIN_MATCH_TYPE, "*");
    }

    public static String filter(String message,String replaceChar){
        return sensitiveWordFilter.replaceSensitiveWord(message, SensitiveWordFilter.MIN_MATCH_TYPE, replaceChar);
    }

}
