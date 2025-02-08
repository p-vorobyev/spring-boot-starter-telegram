package dev.voroby.telegram.message.service.print;

import dev.voroby.telegram.chat.common.Cache;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class PrintService {

    public void print(TdApi.Message message) {
        TdApi.MessageContent content = message.content;
        if (content instanceof TdApi.MessageText mt) {
            if (Cache.idToMainListChat.containsKey(message.chatId)) {
                TdApi.Chat chat = Cache.idToMainListChat.get(message.chatId);
                log.info("Incoming text message:\n[\n\ttitle: {},\n\tmessage: {}\n]",
                        chat.title, mt.text.text);
            }
        }
    }
}
