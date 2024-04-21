package dev.voroby.springframework.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TelegramClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApplication.class, args);
    }

}
