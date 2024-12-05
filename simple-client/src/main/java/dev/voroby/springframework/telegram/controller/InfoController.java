package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.UserTemplate;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import org.drinkless.tdlib.TdApi;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.empty;

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
        Response<TdApi.User> userResponse = telegramClient.send(new TdApi.GetMe());
        return userResponse.getObject().orElseThrow();
    }

    public record Query(String value){}

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
        Response<TdApi.Chats> chatsResponse = telegramClient.send(new TdApi.GetChats(new TdApi.ChatListMain(), 100));
        TdApi.Chats chats = chatsResponse.getObject().orElseThrow();
        return Arrays.stream(chats.chatIds)
                .mapToObj(chatId -> {
                    Response<TdApi.Chat> chatResponse = telegramClient.send(new TdApi.GetChat(chatId));
                    TdApi.Chat chat = chatResponse.getObject().orElseThrow();
                    return chat.title;
                }).toList();
    }

    record Result<T>(Optional<T> object, Optional<TdApi.Error> error) {}

    @GetMapping("/sendHello")
    public void helloToYourself() {
        telegramClient.sendAsync(new TdApi.GetMe())
                .thenApply(this::getActiveUsername)
                .thenApply(this::searchChatByUsername)
                .thenCompose(chatsFuture -> chatsFuture.thenApply(this::getMyChatId))
                .thenAccept(this::sendHelloIfFound);
    }

    private Result<String> getActiveUsername(Response<TdApi.User> userResponse) {
        if (userResponse.getObject().isPresent()) {
            String activeUsername = userResponse.getObject().get().usernames.activeUsernames[0];
            return new Result<>(Optional.of(activeUsername), empty());
        }
        return new Result<>(empty(), userResponse.getError());
    }

    private CompletableFuture<Result<TdApi.Chats>> searchChatByUsername(Result<String> usernameResult) {
        if (usernameResult.object().isPresent()) {
            return telegramClient.sendAsync(new TdApi.SearchChats(usernameResult.object().get(), 1))
                    .thenApply(chatsResponse -> new Result<>(chatsResponse.getObject(), chatsResponse.getError()));
        }
        return CompletableFuture.completedFuture(new Result<>(empty(), usernameResult.error()));
    }

    private Result<Long> getMyChatId(Result<TdApi.Chats> chatsResult) {
        if (chatsResult.object().isPresent()) {
            return new Result<>(Optional.of(chatsResult.object().get().chatIds[0]), empty());
        }
        return new Result<>(empty(), chatsResult.error());
    }

    private void sendHelloIfFound(Result<Long> chatId) {
        if (chatId.object().isPresent()) {
            telegramClient.sendAsync(sendMessageQuery(chatId.object().get()));
        }
    }

    private TdApi.SendMessage sendMessageQuery(Long chatId) {
        var content = new TdApi.InputMessageText();
        var formattedText = new TdApi.FormattedText();
        formattedText.text = "Hello!";
        content.text = formattedText;
        return new TdApi.SendMessage(chatId, 0, null, null, null, content);
    }

}
