package voroby;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.Client.ResultHandler;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public Client client(ResultHandler defaultHandler) {
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        return Client.create(defaultHandler, null, null);
    }

    @Bean
    public ResultHandler defaultHandler() {
        return (TdApi.Object object) -> {
            System.out.println("START HANDLER");
            System.out.println(object.toString());
            System.out.println("END HANDLER");
        };
    }
}
