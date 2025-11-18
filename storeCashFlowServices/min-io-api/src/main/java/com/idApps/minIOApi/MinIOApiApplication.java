package com.idApps.minIOApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinIOApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinIOApiApplication.class, args);
		System.out.println("On est dans la classe principale Main");
	}

}
