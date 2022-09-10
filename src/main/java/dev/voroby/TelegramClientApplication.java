package dev.voroby;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication @Slf4j
public class TelegramClientApplication {

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApplication.class, args);
    }

}
