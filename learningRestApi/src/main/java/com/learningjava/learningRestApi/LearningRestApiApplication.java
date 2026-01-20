package com.learningjava.learningRestApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningRestApiApplication {

	public static void main(String[] args) {

		System.out.println("DB_URL = " + System.getenv("DB_URL"));
		SpringApplication.run(LearningRestApiApplication.class, args);
	}


}
