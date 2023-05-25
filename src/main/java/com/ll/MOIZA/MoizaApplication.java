package com.ll.MOIZA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoizaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoizaApplication.class, args);
	}

}
