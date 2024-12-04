package dev.voroby.springframework.telegram.client

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.voroby.springframework.telegram.client.templates.response.Response
import org.drinkless.tdlib.TdApi
import java.util.concurrent.CompletableFuture

class KTelegramClient(private val delegate: TelegramClient) {

    fun <T : TdApi.Object> send(query: TdApi.Function<T>): Either<TdApi.Error, T> =
        delegate.send(query).toEither()

    fun <T : TdApi.Object> sendAsync(query: TdApi.Function<T>): CompletableFuture<Either<TdApi.Error, T>> =
        delegate.sendAsync(query).thenApply { it.toEither() }

    fun  <T : TdApi.Object> sendWithCallback(
        query: TdApi.Function<T>,
        resultHandler: QueryResultHandler<T>
    ) = delegate.sendWithCallback(query, resultHandler)

    private fun <T : TdApi.Object> Response<T>.toEither() = when {
        `object`.isPresent -> `object`.get().right()
        error.isPresent -> error.get().left()
        else -> TdApi.Error().left()
    }
}

fun TelegramClient.withKotlin() = KTelegramClient(this)