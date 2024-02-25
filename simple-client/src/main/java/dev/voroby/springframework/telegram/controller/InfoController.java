package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.UserTemplate;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class InfoController {

    private final TelegramClient telegramClient;

    private final UserTemplate userTemplate;

    public InfoController(TelegramClient telegramClient,
                          UserTemplate userTemplate) {
        this.telegramClient = telegramClient;
        this.userTemplate = userTemplate;
    }

    @GetMapping("/getMe")
    public TdApi.User getMe() {
        return telegramClient.sendSync(new TdApi.GetMe());
    }

    record Query(String value){}

    @PostMapping(value = "/searchByPhone", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<TdApi.User> searchUserByPhone(@RequestBody Query query) {
        return userTemplate.searchUserByPhoneNumber(query.value).join();
    }

    @PostMapping(value = "/searchByUsername", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<TdApi.User> searchUserByUsername(@RequestBody Query query) {
        return userTemplate.searchUserByUsername(query.value()).join();
    }

    @GetMapping("/chatTitles")
    public List<String> getMyChats() {
        TdApi.Chats chats = telegramClient.sendSync(new TdApi.GetChats(new TdApi.ChatListMain(), 100));
        return Arrays.stream(chats.chatIds)
                .mapToObj(chatId -> {
                    TdApi.Chat chat = telegramClient.sendSync(new TdApi.GetChat(chatId));
                    return chat.title;
                }).toList();
    }

    @GetMapping("/sendHello")
    public void helloToYourself() {
        telegramClient.sendAsync(new TdApi.GetMe())
                .thenApply(user -> user.usernames.activeUsernames[0])
                .thenApply(username -> telegramClient.sendAsync(new TdApi.SearchChats(username, 1)))
                .thenCompose(chatsFuture ->
                        chatsFuture.thenApply(chats -> chats.chatIds[0]))
                .thenApply(chatId -> telegramClient.sendAsync(sendMessageQuery(chatId)));
    }

    private TdApi.SendMessage sendMessageQuery(Long chatId) {
        var content = new TdApi.InputMessageText();
        var formattedText = new TdApi.FormattedText();
        formattedText.text = "Hello!";
        content.text = formattedText;
        return new TdApi.SendMessage(chatId, 0, null, null, null, content);
    }

}
