package com.example.PayAll_DataProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PayAllDataProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayAllDataProviderApplication.class, args);
	}

}
