package dev.voroby.springframework.telegram;

import org.drinkless.tdlib.TdApi;
import org.springframework.boot.ApplicationArguments;

/**
 * Interface indicates that bean should be run when application starts and <b>{@link TdApi.AuthorizationStateReady}</b>
 * received by the client. The order of executions of the registered beans can be controlled
 * with <b>{@link org.springframework.core.annotation.Order}</b> annotation.
 *
 * @author Pavel Vorobyev
 */
public interface TelegramRunner {

    /**
     * Callback used to run the bean.
     * @param args incoming application arguments
     * @throws Exception on error
     */
    void run(ApplicationArguments args) throws Exception;

}
