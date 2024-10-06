package dev.voroby.springframework.telegram

import dev.voroby.springframework.telegram.client.KTelegramClient
import dev.voroby.springframework.telegram.client.TelegramClient
import dev.voroby.springframework.telegram.client.withKotlin
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnBean(TelegramClient::class)
class KotlinTelegramClientAutoConfiguration {

    @Bean
    fun kotlinTelegramClient(telegramClient: TelegramClient): KTelegramClient =
        telegramClient.withKotlin()
}