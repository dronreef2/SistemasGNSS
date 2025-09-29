package com.geosat.gateway.config;

import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2AsyncRequester;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração opcional de cliente HTTP/2 assíncrono (negocia H2/H1).
 * Não substitui ainda o cliente clássico; será usado em paths futuros de streaming.
 */
@Configuration
public class Http2ClientConfig {

    @Bean(destroyMethod = "close")
    public H2AsyncRequester h2AsyncRequester() {
        IOReactorConfig ioConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(15))
                .build();

        H2Config h2Config = H2Config.custom()
                .setPushEnabled(false)
                .setMaxConcurrentStreams(100)
                .build();

        return H2RequesterBootstrap.bootstrap()
                .setIOReactorConfig(ioConfig)
                .setH2Config(h2Config)
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .create();
    }
}
