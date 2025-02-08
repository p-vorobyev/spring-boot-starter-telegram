package dev.voroby.telegram.chat.listeners;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.telegram.chat.common.Cache;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Component;

@Component
public class UpdateNewChat implements UpdateNotificationListener<TdApi.UpdateNewChat> {

    @Override
    public void handleNotification(TdApi.UpdateNewChat notification) {
        TdApi.Chat chat = notification.chat;
        Cache.idToMainListChat.put(chat.id, chat);
    }

    @Override
    public Class<TdApi.UpdateNewChat> notificationType() {
        return TdApi.UpdateNewChat.class;
    }
}
