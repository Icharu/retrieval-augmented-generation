package com.example.ragsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration;

@SpringBootApplication(exclude = {OpenAiChatAutoConfiguration.class, OpenAiEmbeddingAutoConfiguration.class, OpenAiImageAutoConfiguration.class})
public class RagsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagsystemApplication.class, args);
	}

}
