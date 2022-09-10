package dev.voroby.controller;

import dev.voroby.client.TelegramClient;
import dev.voroby.handlers.ChatStateHandler;
import org.drinkless.tdlib.TdApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/info")
public class InfoController {

    private final ChatStateHandler chatStateHandler;

    private final TelegramClient telegramClient;

    public InfoController(ChatStateHandler chatStateHandler,
                          TelegramClient telegramClient) {
        this.chatStateHandler = chatStateHandler;
        this.telegramClient = telegramClient;
    }

    @GetMapping(value = "/chats", produces = APPLICATION_JSON_VALUE)
    public Map<Long, String> getChats() {
        return chatStateHandler.getChatIdToTitle();
    }

    @GetMapping(value = "/getme", produces = APPLICATION_JSON_VALUE)
    public TdApi.User getMe() {
        return telegramClient.getMe();
    }

}
