package cz.tul.stin.paveltyl.quoteapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello SpringBoot, Maven, Lombok, REST!";
    }
}

// Teď jsme poprvé vytvořili vlastní REST endpoint.