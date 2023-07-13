package com.example.grocery_taxi;

import com.example.grocery_taxi.config.CustomCorsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CustomCorsConfiguration.class)
public class GroceryTaxiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroceryTaxiApplication.class, args);
	}
}

