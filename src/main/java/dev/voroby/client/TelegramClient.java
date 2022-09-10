package dev.voroby.client;

import dev.voroby.handlers.ChatStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component @Slf4j
public class TelegramClient {

    private final Client client;

    private volatile boolean haveAuthorization = false;

    private volatile String code;

    private volatile String password;

    private volatile TdApi.User me;

    private final Client.ResultHandler defaultHandler;

    private final ChatStateHandler chatStateHandler;

    public TelegramClient(Client.ResultHandler defaultHandler,
                          ChatStateHandler chatStateHandler) {
        this.defaultHandler = defaultHandler;
        this.chatStateHandler = chatStateHandler;
        Client.execute(new TdApi.SetLogVerbosityLevel(2));
        client = Client.create(new CoreHandler(), null, null);
    }

    @PreDestroy
    void cleanUp() {
        log.info("Goodbye!");
        client.send(new TdApi.Close(), defaultHandler);
    }

    public void updateConfirmationCode(String code) {
        this.code = code;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public boolean haveAuthorization() {
        return haveAuthorization;
    }

    public TdApi.User getMe() {
        return me;
    }

    public Client nativeClient() {
        return client;
    }

    class CoreHandler implements Client.ResultHandler {

        private final Map<Integer, Consumer<TdApi.Object>> tdObjectHandlers;

        private TdApi.AuthorizationState authorizationState;

        public CoreHandler() {
            tdObjectHandlers = Map.of(
                    TdApi.UpdateAuthorizationState.CONSTRUCTOR, this::onAuthorizationStateUpdated,
                    TdApi.UpdateNewChat.CONSTRUCTOR, chatStateHandler::onUpdateNewChat,
                    TdApi.UpdateChatTitle.CONSTRUCTOR, chatStateHandler::onUpdateChatTitle,
                    TdApi.UpdateChatLastMessage.CONSTRUCTOR, chatStateHandler::onUpdateChatLastMessage
            );
        }

        @Override
        public void onResult(TdApi.Object object) {
            tdObjectHandlers.getOrDefault(object.getConstructor(), defaultHandler::onResult)
                    .accept(object);
        }

        @SneakyThrows(InterruptedException.class)
        private void onAuthorizationStateUpdated(TdApi.Object object) {
            var authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
            if (authorizationState != null) {
                this.authorizationState = authorizationState;
            }
            switch (this.authorizationState.getConstructor()) {
                case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                    TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                    parameters.databaseDirectory = System.getenv("DATABASE_DIR");
                    parameters.useMessageDatabase = true;
                    parameters.useSecretChats = true;
                    parameters.apiId = Integer.parseInt(System.getenv("API_ID"));
                    parameters.apiHash = System.getenv("API_HASH");
                    parameters.systemLanguageCode = "en";
                    parameters.deviceModel = "Java_Native";
                    parameters.applicationVersion = "1.8.0";
                    parameters.enableStorageOptimizer = true;
                    client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                    if (Boolean.parseBoolean(System.getenv("ENABLE_MT_PROTO"))) {
                        var mtProto = new TdApi.ProxyTypeMtproto(System.getenv("MT_PROTO_SECRET"));
                        var proxy = new TdApi.AddProxy(System.getenv("PROXY_HOST"), Integer.parseInt(System.getenv("PROXY_PORT")), true, mtProto);
                        client.send(proxy, defaultHandler);
                    }
                }
                case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR ->
                        client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR ->
                        client.send(new TdApi.SetAuthenticationPhoneNumber(System.getenv("PHONE_NUMBER"), null), new AuthorizationRequestHandler());
                case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                    String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
                    log.info("Please confirm this login link on another device: " + link);
                }
                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                    while (checkNullOrBlank(code)) {
                        log.info("Please enter authentication code...");
                        TimeUnit.SECONDS.sleep(3);
                    }
                    client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                }
                case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                    while (checkNullOrBlank(password)) {
                        log.info("Please enter password...");
                        TimeUnit.SECONDS.sleep(3);
                    }
                    client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                }
                case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                    client.send(new TdApi.GetMe(), obj -> me = (TdApi.User) obj);
                    client.send(new TdApi.LoadChats(null, 500), defaultHandler);
                    haveAuthorization = true;
                }
                case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                    haveAuthorization = false;
                    log.info("Logging out");
                }
                case TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                    haveAuthorization = false;
                    log.info("Closing");
                }
                case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> log.info("Closed");
                default -> log.error("Unsupported authorization state:\n" + this.authorizationState);
            }
        }

        private boolean checkNullOrBlank(String field) {
            return field == null || field.isBlank() || field.isEmpty();
        }

        private class AuthorizationRequestHandler implements Client.ResultHandler {
            @Override
            public void onResult(TdApi.Object object) {
                switch (object.getConstructor()) {
                    case TdApi.Error.CONSTRUCTOR -> {
                        log.error("Receive an error:\n" + object);
                        onAuthorizationStateUpdated(null); // repeat last action
                    }
                    // result is already received through UpdateAuthorizationState, nothing to do
                    case TdApi.Ok.CONSTRUCTOR -> {}
                    default -> log.error("Receive wrong response from TDLib:\n" + object);
                }
            }
        }
    }
}
