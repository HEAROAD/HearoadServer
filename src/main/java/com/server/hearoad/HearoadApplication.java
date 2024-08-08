package com.server.hearoad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.server.hearoad")
public class HearoadApplication {

	public static void main(String[] args) {
		SpringApplication.run(HearoadApplication.class, args);
	}

}

