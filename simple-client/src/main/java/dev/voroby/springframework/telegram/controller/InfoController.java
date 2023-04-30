package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class InfoController {

    private final TelegramClient telegramClient;

    public InfoController(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @GetMapping("/getMe")
    public TdApi.User getMe() {
        return telegramClient.sendSync(new TdApi.GetMe(), TdApi.User.class);
    }

    @GetMapping(value = "/chatTitles")
    public List<String> getMyChats() {
        TdApi.Chats chats = telegramClient.sendSync(new TdApi.GetChats(new TdApi.ChatListMain(), 100), TdApi.Chats.class);
        return Arrays.stream(chats.chatIds)
                .mapToObj(chatId -> {
                    TdApi.Chat chat = telegramClient.sendSync(new TdApi.GetChat(chatId), TdApi.Chat.class);
                    return chat.title;
                }).toList();
    }

}
