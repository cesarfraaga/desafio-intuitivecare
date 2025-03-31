package com.intuitivecare.desafio.scraping.config;

import com.intuitivecare.desafio.scraping.ScrapingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public CommandLineRunner run(ScrapingService scrapingService) {
        return args -> scrapingService.executeScraping();
    }
}
