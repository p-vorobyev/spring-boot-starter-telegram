package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TelegramPropertiesTest extends AbstractTest {

    @Autowired
    private TelegramProperties telegramProperties;

    @Test
    void getApi() {
        assertEquals(123, telegramProperties.apiId());
    }

}