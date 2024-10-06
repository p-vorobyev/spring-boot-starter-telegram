package dev.voroby.springframework.telegram.client;

import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import org.drinkless.tdlib.TdApi;

import java.util.function.Consumer;

/**
 * Consumer of incoming TDLib updates from listener.
 *
 * @param <T> listener type
 * @author Pavel Vorobyev
 */
final class UpdateNotificationConsumer<T extends TdApi.Update> implements Consumer<TdApi.Object> {

    private final UpdateNotificationListener<T> notificationListener;

    private final Class<T> type;

    public UpdateNotificationConsumer(UpdateNotificationListener<T> notificationListener, Class<T> clazz) {
        this.notificationListener = notificationListener;
        this.type = clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(TdApi.Object object) {
        T notification = type.cast(object);
        notificationListener.handleNotification(notification);
    }

}
