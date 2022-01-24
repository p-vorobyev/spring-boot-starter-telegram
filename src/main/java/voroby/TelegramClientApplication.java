package voroby;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.Client.ResultHandler;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class TelegramClientApplication {

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private Client client;

    @Autowired
    private ResultHandler defaultHandler;

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApplication.class, args);
    }

    @PreDestroy
    public void cleanUp() {
        System.out.println("Goodbye!");
        client.send(new TdApi.Close(), defaultHandler);
    }

}
