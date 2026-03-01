package com.pdrosoft.matchmaking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan(basePackages = { //
		"com.pdrosoft.matchmaking.controller", //
		"com.pdrosoft.matchmaking.service", //
		"com.pdrosoft.matchmaking.dao", //
		"com.pdrosoft.matchmaking.security", //
		"com.pdrosoft.matchmaking.exception", //

		"com.pdrosoft.matchmaking.stratego.controller", //
		"com.pdrosoft.matchmaking.stratego.service", //
		"com.pdrosoft.matchmaking.stratego.dao", //
		
		"com.pdrosoft.matchmaking.swagger", //
})
public class MatchmakingApplication {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") //
						.allowedOrigins("*") //
						.allowedMethods("*") //
						.allowedHeaders("Authorization", "Content-Type") //
						.exposedHeaders("Authorization");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(MatchmakingApplication.class, args);
	}

}
