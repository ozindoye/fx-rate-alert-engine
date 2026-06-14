package com.ozindoye.fx_alert_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class FxAlertEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FxAlertEngineApplication.class, args);
	}

}
