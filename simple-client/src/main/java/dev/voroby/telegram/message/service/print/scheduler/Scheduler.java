package dev.voroby.telegram.message.service.print.scheduler;

import dev.voroby.telegram.message.common.Cache;
import dev.voroby.telegram.message.service.print.PrintService;
import org.drinkless.tdlib.TdApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

    private final PrintService printService;

    public Scheduler(PrintService printService) {
        this.printService = printService;
    }

    @Scheduled(fixedDelay = 1000)
    private void launch() {
        for (int i = 0; i < 100; i++) {
            TdApi.Message message = Cache.newMessagesQueue.pollFirst();
            if (message == null) {
                break;
            }
            printService.print(message);
        }
    }
}
