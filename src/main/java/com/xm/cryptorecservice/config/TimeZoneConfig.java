package com.xm.cryptorecservice.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuring the default timezone used by the Spring app.
 *
 * @author jason
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));
    }
}
