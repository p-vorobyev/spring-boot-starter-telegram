package dev.voroby.springframework.telegram.client.runner;

import dev.voroby.springframework.telegram.TelegramRunner;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Consumer of {@link TelegramRunner} implementations.
 *
 * @author Pavel Vorobyev
 */
public sealed interface TelegramRunnersConsumer
        extends Consumer<Collection<TelegramRunner>>
        permits TelegramRunnersConsumerImpl {
}
