package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Telegram client component. Wrapper of native {@link Client} with authorization logic and notification handlers.
 *
 * @author Pavel Vorobyev
 */
@Slf4j
public class TelegramClient {

    private final Client client;

    private final Client.ResultHandler defaultHandler;

    /**
     * @param properties TDlib client properties
     * @param notificationHandlers registered notifications handlers
     * @param defaultHandler default handler for unhandled events
     */
    public TelegramClient(TelegramProperties properties,
                          Collection<UpdateNotificationListener<?>> notificationHandlers,
                          Client.ResultHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        this.client = initializeNativeClient(properties, notificationHandlers);
    }

    private Client initializeNativeClient(TelegramProperties properties, Collection<UpdateNotificationListener<?>> notificationHandlers) {
        Client.execute(new TdApi.SetLogVerbosityLevel(properties.logVerbosityLevel()));
        Client.LogMessageHandler logMessageHandler = (level, message) -> {
            switch (level) {
                case 0, 1 -> log.error(message);
                case 2 -> log.warn(message);
                case 3 -> log.info(message);
                default -> log.debug(message);
            }
        };
        Client.setLogMessageHandler(properties.logVerbosityLevel(), logMessageHandler);

        return Client.create(new CoreHandler(notificationHandlers), null, null);
    }

    /**
     * {@link TelegramClient} shutdown hook.
     * Properly closing the client.
     */
    @PreDestroy
    void cleanUp() {
        sendSync(new TdApi.Close());
        log.info("Goodbye!");
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @param type response type
     * @param <T> parametrized response
     * @throws ClassCastException if the object is not null and is not assignable to the type T.
     * @throws NullPointerException if query is null.
     * @return parametrized response from TDLib.
     */
    public <T extends TdApi.Object> T sendSync(TdApi.Function<? extends TdApi.Object> query,
                                               Class<T> type) {
        return type.cast(sendSync(query));
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @throws NullPointerException if query is null.
     * @return response from TDLib.
     */
    public TdApi.Object sendSync(TdApi.Function<? extends TdApi.Object> query) {
        var ref = new AtomicReference<TdApi.Object>();
        client.send(query, ref::set);
        var sent = Instant.now();
        while (ref.get() == null &&
                sent.plus(30, ChronoUnit.SECONDS).isAfter(Instant.now())) {
            /*wait for result*/
        }

        return ref.get();
    }

    /**
     * Sends a request to the TDLib with callback.
     *
     * @param query object representing a query to the TDLib.
     * @param resultHandler Result handler with onResult method which will be called with result
     *                      of the query or with TdApi.Error as parameter.
     * @throws NullPointerException if query is null.
     */
    public void sendWithCallback(TdApi.Function<? extends TdApi.Object> query,
                                 Client.ResultHandler resultHandler) {
        client.send(query, resultHandler);
    }

    /**
     * The main handler for incoming updates from TDLib.
     */
   private final class CoreHandler implements Client.ResultHandler {

        private final Map<Integer, Consumer<TdApi.Object>> tdUpdateHandlers = new HashMap<>();

        private CoreHandler(Collection<UpdateNotificationListener<?>> notifications) {
            notifications.forEach(ntf -> {
                var handler = new UpdateNotificationConsumer(ntf, ntf.notificationType());
                tdUpdateHandlers.putIfAbsent(getConstructorNumberOfType(ntf), handler);
            });
        }

        private int getConstructorNumberOfType(UpdateNotificationListener<?> updateNotification) {
            try {
                TdApi.Update tmp = updateNotification.notificationType().getConstructor().newInstance();
                return tmp.getConstructor();
            } catch (ReflectiveOperationException e) {
                throw new TelegramClientTdApiException(e.getMessage());
            }
        }

       /**
        * {@inheritDoc}
        */
        @Override
        public void onResult(TdApi.Object object) {
            tdUpdateHandlers.getOrDefault(object.getConstructor(), defaultHandler::onResult).
                    accept(object);
        }

    }
}
