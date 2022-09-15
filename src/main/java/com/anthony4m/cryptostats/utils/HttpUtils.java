package com.anthony4m.cryptostats.utils;

import com.anthony4m.cryptostats.config.VaultCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@EnableConfigurationProperties(VaultCredential.class)
public class HttpUtils {
    private static VaultCredential vaultCredential = new VaultCredential();

    public HttpUtils(VaultCredential vaultCredential) {
        HttpUtils.vaultCredential = vaultCredential;
    }

    public static HttpEntity<String> getHttpEntity(){
        HttpHeaders httpHeaders = new HttpHeaders();
        log.info("API Host from utils"+ vaultCredential.getApiHost());
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-RapidAPI-Host",vaultCredential.getApiHost());
        httpHeaders.set("X-RapidAPI-Key",vaultCredential.getApiKey());
        return new HttpEntity<>(null,httpHeaders);
    }

}
