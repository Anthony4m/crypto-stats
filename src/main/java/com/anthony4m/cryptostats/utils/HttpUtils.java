package com.anthony4m.cryptostats.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class HttpUtils {

    private static String apiHost = "coinranking1.p.rapidapi.com";
    private static String apiKey = "f9e0ad06cfmsh7aee569c7bcd32ap1182fcjsnc94caf149f20";
    public static HttpEntity<String> getHttpEntity(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-RapidAPI-Host",apiHost);
        httpHeaders.set("X-RapidAPI-Key",apiKey);
        return new HttpEntity<>(null,httpHeaders);
    }

}
