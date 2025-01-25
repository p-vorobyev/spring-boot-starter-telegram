package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.TelegramClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {TelegramClientAutoConfiguration.class})
public abstract class AbstractTest {

    @MockitoBean
    public TelegramClient telegramClient;

}
