package dev.voroby.springframework.telegram.client.templates;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract class for {@link TelegramClient} templates.
 *
 * @author Pavel Vorobyev
 */
abstract public class AbstractTemplate {

    final TelegramClient telegramClient;

    protected AbstractTemplate(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    <T> T onException(Throwable throwable, AtomicReference<TdApi.Error> errorReference) {
        if (throwable instanceof TelegramClientTdApiException ex) {
            errorReference.set(ex.getError());
            return null;
        }
        throw new RuntimeException(throwable);
    }

    <T extends TdApi.Object> Response<T> createResponse(T object, AtomicReference<TdApi.Error> errorReference) {
        if (errorReference.get() != null) {
            return new Response<>(null, errorReference.get());
        }
        return new Response<>(object, null);
    }

    <T extends TdApi.Object> Response<T> createResponse(T object, TdApi.Error error) {
        if (error != null) {
            return new Response<>(null, error);
        }
        return new Response<>(object, null);
    }

}
