package com.intuitivecare.desafio.transform.config;

import com.intuitivecare.desafio.transform.service.PdfToCsvService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public CommandLineRunner run(PdfToCsvService service) {
        return args -> service.extractTableToCsv();
    }
}
