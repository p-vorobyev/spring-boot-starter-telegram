package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The main handler for incoming updates from TDLib.
 */
final class CoreUpdateHandler implements Client.ResultHandler {

    private final Map<Integer, Consumer<TdApi.Object>> tdUpdateHandlers = new HashMap<>();

    private final Client.ResultHandler defaultHandler;

    CoreUpdateHandler(Collection<UpdateNotificationListener<?>> notifications, Client.ResultHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
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
