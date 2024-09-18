package dev.voroby.springframework.telegram.handlers;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.service.TelegramClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//@Component @Slf4j
public class UpdateNewMessageHandler implements UpdateNotificationListener<TdApi.UpdateNewMessage> {

    private final TelegramClientService telegramService;

    public UpdateNewMessageHandler(TelegramClientService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void handleNotification(TdApi.UpdateNewMessage notification) {
//        telegramService.putMessage(notification.message);
    }

    @Override
    public Class<TdApi.UpdateNewMessage> notificationType() {
        return TdApi.UpdateNewMessage.class;
    }

}
