package dev.voroby.springframework.telegram.client;

/**
 * Interface for handler for results of queries with callback to TDLib.
 * @param <T> The object type that is returned by the function
 *
 * @author Pavel Vorobyev
 */
public interface QueryResultHandler<T extends TdApi.Object> {

    /**
     * Callback called on result of query to TDLib.
     *
     * @param obj Response object from {@link TdApi.Function} query
     * @param error Error result of query to TDLib
     */
    void onResult(T obj, TdApi.Error error);

}
