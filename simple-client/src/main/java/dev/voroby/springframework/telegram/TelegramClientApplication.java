package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication @Slf4j
public class TelegramClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApplication.class, args);
    }

    @Autowired
    private TelegramClient telegramClient;

    @Autowired
    private ClientAuthorizationState authorizationState;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            while (!authorizationState.haveAuthorization()) {/*wait for authorization*/}
            TdApi.LoadChats loadChatsQuery = new TdApi.LoadChats(new TdApi.ChatListMain(), 100);
            telegramClient.sendWithCallback(loadChatsQuery, this::loadChatsHandler);
        };
    }

    public void loadChatsHandler(TdApi.Object object) {
        // https://core.telegram.org/tdlib/docs/classtd_1_1td__api_1_1load_chats.html
        // Returns a 404 error if all chats have been loaded.
        if (object instanceof TdApi.Ok) {
            TdApi.LoadChats loadChatsQuery = new TdApi.LoadChats(new TdApi.ChatListMain(), 100);
            telegramClient.sendWithCallback(loadChatsQuery, this::loadChatsHandler);
        } else {
            log.info("Chats loaded.");
        }
    }

}
