package dev.voroby.springframework.telegram.client.updates

import org.drinkless.tdlib.TdApi

inline fun <reified T : TdApi.Update> listenUpdate(crossinline handler: (T) -> Unit): UpdateNotificationListener<T> =
    object : UpdateNotificationListener<T> {

        override fun handleNotification(notification: T) = handler(notification)

        override fun notificationType(): Class<T> = T::class.java
    }
