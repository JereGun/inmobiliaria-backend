package dev.jgunsett.inmobiliaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InmobiliariaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InmobiliariaApiApplication.class, args);
	}

}
