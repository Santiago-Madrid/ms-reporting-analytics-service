package com.wd.ms_reporting_analytics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsReportingAnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsReportingAnalyticsServiceApplication.class, args);
    }
}
