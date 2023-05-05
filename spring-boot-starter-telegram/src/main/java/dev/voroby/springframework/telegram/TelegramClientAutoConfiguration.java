package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationStateImpl;
import dev.voroby.springframework.telegram.client.updates.UpdateAuthorizationNotification;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;

/**
 * Telegram Spring Boot client AutoConfiguration.
 *
 * @author Pavel Vorobyev
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        prefix = "spring.telegram.client",
        name = {
                "database-encryption-key",
                "api-id",
                "api-hash",
                "phone",
                "system-language-code",
                "device-model"
        }
)
@ConfigurationPropertiesScan(basePackages = "dev.voroby.springframework.telegram.properties")
public class TelegramClientAutoConfiguration {

    //Loading TDLib library
    static {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                System.loadLibrary("libcrypto-1_1-x64");
                System.loadLibrary("libssl-1_1-x64");
                System.loadLibrary("zlib1");
            }
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Autoconfigured telegram client.
     *
     * @param properties {@link TelegramProperties}
     * @param notificationHandlers collection of {@link UpdateNotificationListener} beans
     * @param defaultHandler default handler for incoming updates
     * @return {@link TelegramClient}
     */
    @Bean
    public TelegramClient telegramClient(TelegramProperties properties,
                                         Collection<UpdateNotificationListener<?>> notificationHandlers,
                                         Client.ResultHandler defaultHandler) {
        return new TelegramClient(properties, notificationHandlers, defaultHandler);
    }

    /**
     * Client authorization state.
     *
     * @return {@link ClientAuthorizationState}
     */
    @Bean
    public ClientAuthorizationState clientAuthorizationState() {
        return new ClientAuthorizationStateImpl();
    }

    /**
     * Notification listener for authorization sate change.
     *
     * @return {@link UpdateNotificationListener<TdApi.UpdateAuthorizationState>}
     */
    @Bean
    public UpdateNotificationListener<TdApi.UpdateAuthorizationState> updateAuthorizationNotification(TelegramProperties properties,
                                                                                                      @Lazy TelegramClient telegramClient,
                                                                                                      ClientAuthorizationState clientAuthorizationState) {
        return new UpdateAuthorizationNotification(properties, telegramClient, clientAuthorizationState);
    }

    /**
     * @return Default handler for incoming TDLib updates.
     * Could be overwritten by another bean
     */
    @Bean
    public Client.ResultHandler defaultHandler() {
        return (TdApi.Object object) ->
                log.debug("\nSTART DEFAULT HANDLER\n" +
                        object.toString() + "\n" +
                        "END DEFAULT HANDLER");
    }

}
