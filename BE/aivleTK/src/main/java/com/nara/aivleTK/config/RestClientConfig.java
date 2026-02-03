package com.nara.aivleTK.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient pythonRestClient(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000);
        requestFactory.setReadTimeout(120000);

        return RestClient.builder()
                .baseUrl("https://aivleachatbot.greenpond-9eab36ab.koreacentral.azurecontainerapps.io")
                .requestFactory(requestFactory)
                .build();
    }
}
