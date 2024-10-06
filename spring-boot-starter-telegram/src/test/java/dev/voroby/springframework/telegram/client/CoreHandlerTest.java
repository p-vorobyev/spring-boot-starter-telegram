package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreHandlerTest {

    @Test
    void onResult() {
        var actualValue = new AtomicInteger();
        var updateNewChatListener = new UpdateNotificationListener<TdApi.UpdateNewChat>() {
            @Override
            public void handleNotification(TdApi.UpdateNewChat notification) {
                actualValue.set(notification.getConstructor());
            }

            @Override
            public Class<TdApi.UpdateNewChat> notificationType() {
                return TdApi.UpdateNewChat.class;
            }
        };

        var coreHandler = new CoreUpdateHandler(List.of(updateNewChatListener), obj -> {});
        var updateNewChat = new TdApi.UpdateNewChat();
        coreHandler.onResult(updateNewChat);

        assertEquals(updateNewChat.getConstructor(), actualValue.get());
    }

}