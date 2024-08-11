package com.server.hearoad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HearoadApplication {

	public static void main(String[] args) {
		SpringApplication.run(HearoadApplication.class, args);
	}

}

