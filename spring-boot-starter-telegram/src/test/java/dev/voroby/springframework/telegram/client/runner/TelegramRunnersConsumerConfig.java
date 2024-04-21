package dev.voroby.springframework.telegram.client.runner;

import dev.voroby.springframework.telegram.TelegramRunner;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationStateImpl;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class TelegramRunnersConsumerConfig {

    @Bean @Order(0)
    public TelegramRunner firstRunner() {
        return TelegramRunnersConsumerTest.first;
    }

    @Bean @Order(1)
    public TelegramRunner secondRunner() {
        return TelegramRunnersConsumerTest.second;
    }

    @Bean @Order(2)
    public TelegramRunner thirdRunner() {
        return TelegramRunnersConsumerTest.third;
    }

    @Bean
    public TelegramRunnersConsumer telegramRunnersConsumer(ApplicationArguments applicationArguments,
                                                           ApplicationContext applicationContext) {
        var authorizationState = Mockito.mock(ClientAuthorizationStateImpl.class);
        Mockito.when(authorizationState.haveAuthorization()).thenReturn(true);
        return new TelegramRunnersConsumerImpl(authorizationState, applicationArguments, applicationContext);
    }

}
