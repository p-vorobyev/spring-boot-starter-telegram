package dev.voroby.springframework.telegram.service;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service @Slf4j
public class TelegramClientService {

    private final TelegramClient telegramClient;

    private final Deque<TdApi.Message> messages = new ConcurrentLinkedDeque<>();

    public TelegramClientService(@Lazy TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void putMessage(TdApi.Message msg) {
        messages.addLast(msg);
    }

    @Scheduled(fixedDelay = 1000)
    private void handleMessages() {
        for (int i = 0; i < 100; i++) {
            TdApi.Message message = messages.pollFirst();
            if (message == null) {
                break;
            }
            TdApi.MessageContent content = message.content;
            if (content instanceof TdApi.MessageText mt) {
                TdApi.Chat chat = telegramClient.sendSync(new TdApi.GetChat(message.chatId), TdApi.Chat.class);
                log.info("Incoming text message:\n[\n\ttitle: {},\n\tmessage: {}\n]", chat.title, mt.text.text);
            }
        }
    }

}
