package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreHandlerTest {

    @Test
    void onResult() {
        var notificationNewChatConstructor = new AtomicInteger();
        UpdateNotificationListener<TdApi.UpdateNewChat> updateNewChatNotification = new UpdateNotificationListener<>() {
            @Override
            public void handleNotification(TdApi.UpdateNewChat notification) {
                notificationNewChatConstructor.set(notification.getConstructor());
            }

            @Override
            public Class<TdApi.UpdateNewChat> notificationType() {
                return TdApi.UpdateNewChat.class;
            }
        };

        var coreHandler = new CoreHandler(List.of(updateNewChatNotification), obj -> {});
        var updateNewChat = new TdApi.UpdateNewChat();
        coreHandler.onResult(updateNewChat);

        assertEquals(updateNewChat.getConstructor(), notificationNewChatConstructor.get());
    }

}