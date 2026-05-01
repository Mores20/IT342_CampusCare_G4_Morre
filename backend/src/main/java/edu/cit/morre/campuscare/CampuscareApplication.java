package edu.cit.morre.campuscare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class  CampuscareApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampuscareApplication.class, args);
	}

}
