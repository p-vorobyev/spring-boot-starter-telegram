package dev.voroby.springframework.telegram.client.templates.response;

import org.drinkless.tdlib.TdApi;

/**
 * Response wrapper for queries.
 *
 * @param object {@link TdApi.Object} Query response.
 * @param error {@link TdApi.Error} Query error.
 * @param <T>
 * @author Pavel Vorobyev
 */
public record Response<T extends TdApi.Object>(
        T object,
        TdApi.Error error
) {
}
