package dev.voroby.telegram.message.common;

import org.drinkless.tdlib.TdApi;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Cache {

    public static final Deque<TdApi.Message> newMessagesQueue = new ConcurrentLinkedDeque<>();
}
