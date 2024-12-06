package dev.voroby.springframework.telegram.client.templates.response;

import org.drinkless.tdlib.TdApi;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Response wrapper for queries.
 *
 * @author Pavel Vorobyev
 */
public final class Response<T extends TdApi.Object> {
    private final T object;
    private final TdApi.Error error;

    /**
     * @param object {@link TdApi.Object} Query response.
     * @param error  {@link TdApi.Error} Query error.
     */
    public Response(T object, TdApi.Error error) {
        this.object = object;
        this.error = error;
    }

    /**
     * Map, or transform, the {@link TdApi.Object} if it exists inside {@link Response}
     * otherwise return {@link Response} with existing {@link TdApi.Error}.
     * @param mapFunction function to transform {@link TdApi.Object} in response
     * @param <R> type of new {@link TdApi.Object}
     * @return {@link Response<R>}
     */
    public <R extends TdApi.Object> Response<R> map(Function<T, R> mapFunction) {
        if (object != null) {
            return new Response<>(mapFunction.apply(object), null);
        }
        return new Response<>(null, error);
    }

    /**
     * Performs an action upon a successful function call to TDLib and returns current {@link Response<T>}.
     * @param action callback called upon a successful function call of query to TDLib
     * @return {@link Response<T>}
     */
    public Response<T> onSuccess(Consumer<T> action) {
        if (object != null) {
            action.accept(object);
        }
        return this;
    }

    /**
     * Performs an action in case of an error and returns current {@link Response<T>}.
     * @param action callback called in case of an error of query to TDLib
     * @return {@link Response<T>}
     */
    public Response<T> onError(Consumer<TdApi.Error> action) {
        if (error != null) {
            action.accept(error);
        }
        return this;
    }

    public Optional<T> getObject() {
        return Optional.ofNullable(object);
    }

    public Optional<TdApi.Error> getError() {
        return Optional.ofNullable(error);
    }
}
