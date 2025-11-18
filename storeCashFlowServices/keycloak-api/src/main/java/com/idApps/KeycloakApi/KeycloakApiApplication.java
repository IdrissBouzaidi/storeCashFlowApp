package com.idApps.KeycloakApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KeycloakApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakApiApplication.class, args);
		System.out.println("On est dans la classe principale Main");
	}

}
