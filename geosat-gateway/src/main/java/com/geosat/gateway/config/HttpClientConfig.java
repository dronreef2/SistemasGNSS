package com.geosat.gateway.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient closeableHttpClient(
            @Value("${rbmc.timeouts.connect-ms:3000}") int connectMs,
            @Value("${rbmc.timeouts.response-ms:10000}") int responseMs,
            @Value("${rbmc.user-agent:GeoSatGateway/0.1}") String userAgent
    ) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectMs))
                .setResponseTimeout(Timeout.ofMilliseconds(responseMs))
                .build();

        return HttpClients.custom()
                .setUserAgent(userAgent)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
