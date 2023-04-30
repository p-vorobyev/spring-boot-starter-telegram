package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {TelegramClientAutoConfiguration.class})
public class TelegramClientAutoConfigurationTest {

    @MockBean
    private TelegramClient telegramClient;

    @Autowired
    private TelegramProperties telegramProperties;

    @Test
    void contextStartupTest() {
        assertEquals(123, telegramProperties.apiId());
    }

}