package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UpdateNotificationConsumerTest {

    private final UpdateNotificationConsumer<TdApi.UpdateNewChat> notificationConsumer;

    private final UpdateNotificationListener<TdApi.UpdateNewChat> notificationListener;

    public UpdateNotificationConsumerTest() {
        notificationListener = mock(UpdateNotificationListener.class);
        notificationConsumer = new UpdateNotificationConsumer<>(notificationListener, TdApi.UpdateNewChat.class);
    }

    @Test
    void accept() {
        var updateNewChat = new TdApi.UpdateNewChat();
        notificationConsumer.accept(updateNewChat);
        verify(notificationListener).handleNotification(updateNewChat);
    }

    @Test
    void acceptTypeError() {
        var updateNewMessage = new TdApi.UpdateNewMessage();
        assertThrows(ClassCastException.class, () -> notificationConsumer.accept(updateNewMessage));
    }

    @Test
    void acceptNull() {
        ArgumentCaptor<TdApi.UpdateNewChat> captor = ArgumentCaptor.forClass(TdApi.UpdateNewChat.class);
        notificationConsumer.accept(null);
        verify(notificationListener).handleNotification(captor.capture());
        assertNull(captor.getValue());
    }
}