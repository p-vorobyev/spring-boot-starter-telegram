package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.templates.response.Response;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.exception.TelegramClientConfigurationException;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import jakarta.annotation.PreDestroy;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.util.StringUtils.hasText;

/**
 * Telegram client component. Wrapper of native {@link Client} with authorization logic and notification handlers.
 *
 * @author Pavel Vorobyev
 */
public class TelegramClient {

    private final Logger log = LoggerFactory.getLogger(TelegramClient.class);

    private final Client client;

    private final Client.ResultHandler defaultHandler;

    private final ClientAuthorizationState clientAuthorizationState;

    /**
     * @param properties TDlib client properties
     * @param notificationHandlers registered notifications handlers
     * @param defaultHandler default handler for unhandled events
     * @param clientAuthorizationState authorization state of the client
     */
    public TelegramClient(TelegramProperties properties,
                          Collection<UpdateNotificationListener<?>> notificationHandlers,
                          Client.ResultHandler defaultHandler,
                          ClientAuthorizationState clientAuthorizationState) {
        this.defaultHandler = defaultHandler;
        checkProperties(properties);
        this.clientAuthorizationState = clientAuthorizationState;
        this.client = initializeNativeClient(properties, notificationHandlers);
    }

    private void checkProperties(TelegramProperties properties) {
        if (properties.phone() == null) {
            throw new TelegramClientConfigurationException("The phone number of the user not filled. " +
                    "Specify property spring.telegram.client.phone");
        }
        if (properties.apiId() == 0) {
            throw new TelegramClientConfigurationException("Application identifier for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-id");
        }
        if (!hasText(properties.apiHash())) {
            throw new TelegramClientConfigurationException("Application identifier hash for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-hash");
        }
        if (!hasText(properties.databaseEncryptionKey())) {
            throw new TelegramClientConfigurationException("Encryption key for the database is invalid. " +
                    "Specify property spring.telegram.client.database-encryption-key");
        }
        if (!hasText(properties.systemLanguageCode())) {
            throw new TelegramClientConfigurationException("IETF language tag of the user's operating system language; must be non-empty. " +
                    "Specify property spring.telegram.client.system-language-code");
        }
        if (!hasText(properties.deviceModel())) {
            throw new TelegramClientConfigurationException("Model of the device the application is being run on; must be non-empty. " +
                    "Specify property spring.telegram.client.device-model");
        }
        TelegramProperties.Proxy proxy = properties.proxy();
        if (proxy != null) {
            checkProxyProperties(proxy);
        }
    }

    private static void checkProxyProperties(TelegramProperties.Proxy proxy) {
        if (!hasText(proxy.server()) || proxy.port() <= 0) {
            throw new TelegramClientConfigurationException("""
                    Proxy settings not filled. Specify properties:
                     spring.telegram.client.proxy.server
                     spring.telegram.client.proxy.port
                    """);
        }
        TelegramProperties.Proxy.ProxyHttp http = proxy.http();
        TelegramProperties.Proxy.ProxySocks5 socks5 = proxy.socks5();
        TelegramProperties.Proxy.ProxyMtProto mtProto = proxy.mtproto();
        if (http != null) {
            if (!hasText(http.password()) || !hasText(http.username())) {
                throw new TelegramClientConfigurationException("""
                        Http proxy settings not filled. Specify properties:
                         spring.telegram.client.proxy.http.username
                         spring.telegram.client.proxy.http.password
                         spring.telegram.client.proxy.http.http-only
                         """);
            }
        } else if (socks5 != null) {
            if (!hasText(socks5.username()) || !hasText(socks5.password())) {
                throw new TelegramClientConfigurationException("""
                        Socks5 proxy settings not filled. Specify properties:
                         spring.telegram.client.proxy.socks5.username
                         spring.telegram.client.proxy.socks5.password
                         """);
            }
        } else if (mtProto != null) {
            if (!hasText(mtProto.secret())) {
                throw new TelegramClientConfigurationException("MtProto proxy settings not filled. " +
                        "Specify property spring.telegram.client.proxy.mtProto.secret");
            }
        } else {
            throw new TelegramClientConfigurationException("ProxyType not filled. Available types - http, socks5, mtproto");
        }
    }

    private Client initializeNativeClient(TelegramProperties properties, Collection<UpdateNotificationListener<?>> notificationHandlers) {
        var logVerbosityLevel = new TdApi.SetLogVerbosityLevel(properties.logVerbosityLevel());
        try {
            Client.execute(logVerbosityLevel);
        } catch (Client.ExecutionException e) {
            logError(logVerbosityLevel, e.error);
            throw new RuntimeException(e);
        }
        Client.LogMessageHandler logMessageHandler = (level, message) -> {
            switch (level) {
                case 0, 1 -> log.error(message);
                case 2 -> log.warn(message);
                case 3 -> log.info(message);
                default -> log.debug(message);
            }
        };
        Client.setLogMessageHandler(properties.logVerbosityLevel(), logMessageHandler);

        return Client.create(new CoreUpdateHandler(notificationHandlers, defaultHandler), null, null);
    }

    /**
     * {@link TelegramClient} shutdown hook.
     * Properly closing the client.
     */
    @PreDestroy
    void cleanUp() throws InterruptedException {
        send(new TdApi.Close());
        Instant startAwait = Instant.now();
        while (!clientAuthorizationState.isStateClosed() && startAwait.plusSeconds(30).isAfter(Instant.now())) {
            TimeUnit.MILLISECONDS.sleep(200);
        }
        if (!clientAuthorizationState.isStateClosed()) {
            log.warn("Closed, but TDLib client isn't in its final state");
        }
        log.info("Goodbye!");
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @throws NullPointerException if query is null.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     * @return response from TDLib.
     * @deprecated Because of this method throws a RuntimeException when we get an error from TDLib. It's better
     * to return a value object. Can be replaced by
     *
     * <pre>{@code
     *  telegramClient.send(TdApi.Function<T> query)
     * }</pre>
     *
     */
    @Deprecated(since = "1.13.0")
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> T sendSync(TdApi.Function<T> query) {
        Objects.requireNonNull(query);
        var ref = new AtomicReference<TdApi.Object>();
        client.send(query, ref::set);
        var sent = Instant.now();
        while (ref.get() == null &&
                sent.plus(60, ChronoUnit.SECONDS).isAfter(Instant.now())) {
            /*wait for result*/
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            }
        }

        TdApi.Object obj = ref.get();
        if (obj == null) {
            throw new TelegramClientTdApiException("TDLib request timeout.");
        }
        if (obj instanceof TdApi.Error err) {
            logError(query, err);
            throw new TelegramClientTdApiException("Received an error from TDLib.", err, query);
        }

        return (T) obj;
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @throws NullPointerException if query is null.
     * @return {@link Response<T>} response.
     */
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> Response<T> send(TdApi.Function<T> query) {
        Objects.requireNonNull(query);
        var ref = new AtomicReference<TdApi.Object>();
        client.send(query, ref::set);
        var sent = Instant.now();
        while (ref.get() == null &&
                sent.plus(30, ChronoUnit.SECONDS).isAfter(Instant.now())) {
            /*wait for result*/
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            }
        }

        TdApi.Object obj = ref.get();
        if (obj == null) {
            var error = new TdApi.Error(0, "TDLib request timeout.");
            logError(query, error);
            return new Response<>(null, error);
        } else if (obj instanceof TdApi.Error err) {
            logError(query, err);
            return new Response<>(null, err);
        }

        return new Response<>((T) obj, null);
    }

    /**
     * Sends a request to the TDLib asynchronously.
     * If this stage completes exceptionally you can handle cause {@link TelegramClientTdApiException}
     *
     * @throws NullPointerException if query is null.
     * @param query object representing a query to the TDLib.
     * @return {@link CompletableFuture<Response>} response from TDLib.
     */
    public <T extends TdApi.Object> CompletableFuture<Response<T>> sendAsync(TdApi.Function<T> query) {
        Objects.requireNonNull(query);
        var future = new CompletableFuture<Response<T>>();
        sendWithCallback(query, ((obj, error) -> {
            if (error != null) {
                logError(query, error);
            }
            future.complete(new Response<>(obj, error));
        }));
        return future;
    }

    private void logError(TdApi.Function<?> query, TdApi.Error error) {
        String errorLogString = String.format("""
                TDLib error:
                [
                    code: %d,
                    message: %s
                    queryIdentifier: %d
                ]
                """, error.code, error.message, query.getConstructor());
        log.error(errorLogString);
    }


    /**
     * Sends a request to the TDLib with callback.
     *
     * @param query object representing a query to the TDLib
     * @param resultHandler Result handler for results of queries with callback to TDLib
     * @param <T> The object type that is returned by the function
     */
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> void sendWithCallback(TdApi.Function<T> query,
                                                          QueryResultHandler<T> resultHandler) {
        Objects.requireNonNull(query);
        client.send(query, object -> {
            if (object instanceof TdApi.Error err) {
                resultHandler.onResult(null, err);
            } else {
                resultHandler.onResult((T) object, null);
            }
        });
    }

}
