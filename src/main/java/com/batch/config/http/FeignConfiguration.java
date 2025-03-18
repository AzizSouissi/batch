package com.batch.config.http;


import feign.Logger;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
@Slf4j
@Configuration
public class FeignConfiguration {

    private static final Duration CONNECT_TIMEOUT_DURATION = Duration.ofSeconds(30);
    private static final Duration READ_TIMEOUT_DURATION = Duration.ofSeconds(30);

    @Bean
    public Request.Options requestOptions() {
        log.info("[Feign] Initializing global timeouts configuration");
        return new Request.Options(CONNECT_TIMEOUT_DURATION, READ_TIMEOUT_DURATION, true);
    }

    /**
     * https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#feign-logging
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        log.info("[Feign] Initializing logging level");
        return Logger.Level.FULL;
    }
}

