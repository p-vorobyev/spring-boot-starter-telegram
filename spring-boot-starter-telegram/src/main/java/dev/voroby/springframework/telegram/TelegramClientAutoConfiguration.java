package dev.voroby.springframework.telegram;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.runner.TelegramRunnersConsumer;
import dev.voroby.springframework.telegram.client.runner.TelegramRunnersConsumerImpl;
import dev.voroby.springframework.telegram.client.templates.ChatTemplate;
import dev.voroby.springframework.telegram.client.templates.UserTemplate;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationStateImpl;
import dev.voroby.springframework.telegram.client.updates.UpdateAuthorizationState;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;

/**
 * Telegram Spring Boot client AutoConfiguration.
 *
 * @author Pavel Vorobyev
 */
@Configuration
@ConfigurationPropertiesScan(basePackages = "dev.voroby.springframework.telegram.properties")
public class TelegramClientAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(TelegramClientAutoConfiguration.class);

    //Loading TDLib library
    static {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                System.loadLibrary("libcrypto-3-x64");
                System.loadLibrary("libssl-3-x64");
                System.loadLibrary("zlib1");
            }
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Autoconfigured telegram client.
     *
     * @param properties {@link TelegramProperties}
     * @param notificationHandlers collection of {@link UpdateNotificationListener} beans
     * @param defaultHandler default handler for incoming updates
     * @param clientAuthorizationState authorization state of the client
     * @return {@link TelegramClient}
     */
    @Bean
    public TelegramClient telegramClient(TelegramProperties properties,
                                         Collection<UpdateNotificationListener<?>> notificationHandlers,
                                         Client.ResultHandler defaultHandler,
                                         ClientAuthorizationState clientAuthorizationState) {
        return new TelegramClient(properties, notificationHandlers, defaultHandler, clientAuthorizationState);
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
                                                                                                      @Lazy TelegramClient telegramClient) {
        return new UpdateAuthorizationState(properties, telegramClient);
    }

    /**
     * Template for {@link TdApi.User} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link UserTemplate}.
     */
    @Bean
    public UserTemplate userTemplate(@Lazy TelegramClient telegramClient) {
        return new UserTemplate(telegramClient);
    }

    /**
     * Template for {@link TdApi.Chat} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link ChatTemplate}.
     */
    @Bean
    public ChatTemplate chatTemplate(@Lazy TelegramClient telegramClient) {
        return new ChatTemplate(telegramClient);
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

    /**
     * Creates a consumer of {@link TelegramRunner} implementations.
     *
     * @param authorizationState authorization state of the client
     * @param applicationArguments the arguments that were used to run a {@link SpringApplication}
     * @param applicationContext interface to provide configuration for an spring application
     * @return {@link TelegramRunnersConsumer}
     */
    @Bean
    public TelegramRunnersConsumer telegramRunnersConsumer(ClientAuthorizationState authorizationState,
                                                           ApplicationArguments applicationArguments,
                                                           ApplicationContext applicationContext) {
        return new TelegramRunnersConsumerImpl(authorizationState, applicationArguments, applicationContext);
    }

    /**
     * @param telegramRunners collection of {@link TelegramRunner} implementations
     * @param telegramRunnersConsumer consumer of {@link TelegramRunner} implementations
     * @return {@link ApplicationRunner}
     */
    @Bean
    public ApplicationRunner telegramClientRunner(Collection<TelegramRunner> telegramRunners,
                                                  TelegramRunnersConsumer telegramRunnersConsumer) {
        return args -> telegramRunnersConsumer.accept(telegramRunners);
    }

}
