package dev.voroby.telegram.chat.common;

import org.drinkless.tdlib.TdApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Cache {

    public static Map<Long, TdApi.Chat> idToMainListChat = new ConcurrentHashMap<>();
}
