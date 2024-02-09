package com.example.spector;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAdminServer
@SpringBootApplication
public class SpectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpectorApplication.class, args);
	}

}
