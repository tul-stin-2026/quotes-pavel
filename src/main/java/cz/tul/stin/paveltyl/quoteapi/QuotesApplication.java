package cz.tul.stin.paveltyl.quoteapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class QuotesApplication {

    static void main(String[] args) {
        SpringApplication.run(QuotesApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() { // = nástroj pro HTTP volání
        return new RestTemplate();
    }
}
