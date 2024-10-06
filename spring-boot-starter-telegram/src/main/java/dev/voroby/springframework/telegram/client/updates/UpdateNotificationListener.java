package dev.voroby.springframework.telegram.client.updates;

import org.drinkless.tdlib.TdApi;

/**
 * Interface for incoming updates from TDLib.
 * @param <T> type of update
 *
 * @author Vorobyev Pavel
 */
public interface UpdateNotificationListener<T extends TdApi.Update> {

    /**
     * Handles incoming update event.
     *
     * @param notification incoming update from TDLib
     */
    void handleNotification(T notification);

    /**
     * @return listener class type
     */
    Class<T> notificationType();

}
