package com.mongoacl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
//TODO: нужно ли это здесь - аннотации configuration и componentscan?
@Configuration
@ComponentScan
public class MongoAclApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoAclApplication.class, args);
	}
}
