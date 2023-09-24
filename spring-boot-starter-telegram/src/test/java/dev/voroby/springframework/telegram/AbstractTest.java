package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.TelegramClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {TelegramClientAutoConfiguration.class})
public abstract class AbstractTest {

    @MockBean
    public TelegramClient telegramClient;

}
