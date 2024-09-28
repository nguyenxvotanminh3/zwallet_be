package com.nguyenminh.microservices.zwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


import java.time.Duration;

@SpringBootApplication
@EnableCaching
public class ZwalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZwalletApplication.class, args);
	}

	}
