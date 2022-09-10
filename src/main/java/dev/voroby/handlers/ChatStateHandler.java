package dev.voroby.handlers;

import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChatStateHandler {

    private final Map<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();

    public void onUpdateNewChat(TdApi.Object object) {
        TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
        TdApi.Chat chat = updateNewChat.chat;
        chats.put(chat.id, chat);
    }

    public void onUpdateChatTitle(TdApi.Object object) {
        TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
        TdApi.Chat chat = chats.get(updateChat.chatId);
        synchronized (chat) {
            chat.title = updateChat.title;
        }
    }

    public void onUpdateChatLastMessage(TdApi.Object object) {
        TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
        TdApi.Chat chat = chats.get(updateChat.chatId);
        synchronized (chat) {
            chat.lastMessage = updateChat.lastMessage;
        }
    }

    public Map<Long, String> getChatIdToTitle() {
        return chats.values()
                .stream()
                .filter(chat -> chat.title != null && !chat.title.isEmpty())
                .collect(Collectors.toMap(c -> c.id, c -> c.title));
    }

}
