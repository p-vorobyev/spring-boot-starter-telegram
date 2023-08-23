package dev.voroby.springframework.telegram.properties;

import dev.voroby.springframework.telegram.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TelegramPropertiesTest extends AbstractTest {

    @Autowired
    private TelegramProperties telegramProperties;

    @Value("${mtproto.secret.test}")
    private String testSecret;

    @Test
    void getApi() {
        assertEquals(123, telegramProperties.apiId());
    }

    @Test
    void getMtprotoSecret() {
        String secret = telegramProperties.proxy().mtproto().secret();
        assertEquals(testSecret, secret);
    }

}