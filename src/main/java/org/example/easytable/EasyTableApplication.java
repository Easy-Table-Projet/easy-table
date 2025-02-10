package org.example.easytable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EasyTableApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyTableApplication.class, args);
    }

}
