package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.exception.TelegramClientConfigurationException;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.StringUtils.hasText;

/**
 * Handler of {@link TdApi.AuthorizationState} updates.
 */
@Slf4j
public class UpdateAuthorizationNotification implements UpdateNotificationListener<TdApi.UpdateAuthorizationState> {

    private TdApi.AuthorizationState authorizationState;

    private final TelegramProperties properties;

    private final TelegramClient telegramClient;

    private final ClientAuthorizationStateImpl clientAuthorizationState;

    public UpdateAuthorizationNotification(TelegramProperties properties,
                                           TelegramClient telegramClient,
                                           ClientAuthorizationState clientAuthorizationState) {
        this.properties = properties;
        this.telegramClient = telegramClient;
        this.clientAuthorizationState = (ClientAuthorizationStateImpl) clientAuthorizationState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows(InterruptedException.class)
    public void handleNotification(TdApi.UpdateAuthorizationState notification) {
        if (notification != null) {
            TdApi.AuthorizationState newAuthorizationState = notification.authorizationState;
            if (newAuthorizationState != null) {
                this.authorizationState = newAuthorizationState;
            }
        }
        switch (this.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                telegramClient.sendWithCallback(tdlibParameters(), new AuthorizationRequestHandler());
                TelegramProperties.Proxy proxy = properties.proxy();
                if (proxy != null) {
                    addProxy(proxy);
                }
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                String phone = properties.phone();
                if (phone == null) {
                    throw new TelegramClientConfigurationException("The phone number of the user not filled. " +
                            "Specify property spring.telegram.client.phone");
                }
                telegramClient.sendWithCallback(new TdApi.SetAuthenticationPhoneNumber(phone, null), new AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
                log.info("Please confirm this login link on another device: " + link);
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                try {
                    if (!hasText(clientAuthorizationState.getCode())) {
                        clientAuthorizationState.setWaitAuthenticationCode();
                        while (!hasText(clientAuthorizationState.getCode())) {
                            log.info("Please enter authentication code");
                            TimeUnit.SECONDS.sleep(3);
                        }
                    }
                    telegramClient.sendWithCallback(new TdApi.CheckAuthenticationCode(clientAuthorizationState.getCode()), new AuthorizationRequestHandler());
                } finally {
                    clientAuthorizationState.clearCode();
                }
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                try {
                    if (!hasText(clientAuthorizationState.getPassword())) {
                        clientAuthorizationState.setWaitAuthenticationPassword();
                        while (!hasText(clientAuthorizationState.getPassword())) {
                            log.info("Please enter password");
                            TimeUnit.SECONDS.sleep(3);
                        }
                    }
                    telegramClient.sendWithCallback(new TdApi.CheckAuthenticationPassword(clientAuthorizationState.getPassword()), new AuthorizationRequestHandler());
                } finally {
                    clientAuthorizationState.clearPassword();
                }
            }
            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
                try {
                    if (!hasText(clientAuthorizationState.getEmailAddress())) {
                        clientAuthorizationState.setWaitEmailAddress();
                        while (!hasText(clientAuthorizationState.getEmailAddress())) {
                            log.info("Please enter email");
                            TimeUnit.SECONDS.sleep(3);
                        }
                    }
                    telegramClient.sendWithCallback(new TdApi.SetAuthenticationEmailAddress(clientAuthorizationState.getEmailAddress()), new AuthorizationRequestHandler());
                } finally {
                    clientAuthorizationState.clearEmailAddress();
                }
            }
            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
                try {
                    if (!hasText(clientAuthorizationState.getCode())) {
                        clientAuthorizationState.setWaitAuthenticationCode();
                        while (!hasText(clientAuthorizationState.getCode())) {
                            log.info("Please enter authentication code");
                            TimeUnit.SECONDS.sleep(3);
                        }
                    }
                    var emailAuth = new TdApi.EmailAddressAuthenticationCode(clientAuthorizationState.getCode());
                    telegramClient.sendWithCallback(new TdApi.CheckAuthenticationEmailCode(emailAuth), new AuthorizationRequestHandler());
                } finally {
                    clientAuthorizationState.clearCode();
                }
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR ->
                    clientAuthorizationState.setHaveAuthorization(true);
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                clientAuthorizationState.setHaveAuthorization(false);
                log.info("Logging out");
            }
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                clientAuthorizationState.setHaveAuthorization(false);
                log.info("Closing");
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> log.info("Closed");
            default -> log.error("Unsupported authorization state:\n" + this.authorizationState);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<TdApi.UpdateAuthorizationState> notificationType() {
        return TdApi.UpdateAuthorizationState.class;
    }

    /**
     * Configure TDLib parameters.
     * @return {@link TdApi.SetTdlibParameters}
     */
    private TdApi.SetTdlibParameters tdlibParameters() {
        boolean useTestDc = properties.useTestDc();
        String databaseDirectory = checkStringOrEmpty(properties.databaseDirectory());
        String filesDirectory = checkStringOrEmpty(properties.filesDirectory());
        if (!hasText(properties.databaseEncryptionKey())) {
            throw new TelegramClientConfigurationException("Encryption key for the database is invalid. " +
                    "Specify property spring.telegram.client.database-encryption-key");
        }
        byte[] databaseEncryptionKey = properties.databaseEncryptionKey().getBytes(StandardCharsets.UTF_8);
        boolean useFileDatabase = properties.useFileDatabase();
        boolean useChatInfoDatabase = properties.useChatInfoDatabase();
        boolean useMessageDatabase = properties.useMessageDatabase();
        boolean useSecretChats = properties.useSecretChats();
        if (properties.apiId() == 0) {
            throw new TelegramClientConfigurationException("Application identifier for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-id");
        }
        int apiId = properties.apiId();
        if (!hasText(properties.apiHash())) {
            throw new TelegramClientConfigurationException("Application identifier hash for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-hash");
        }
        String apiHash = properties.apiHash();
        if (!hasText(properties.systemLanguageCode())) {
            throw new TelegramClientConfigurationException("IETF language tag of the user's operating system language; must be non-empty. " +
                    "Specify property spring.telegram.client.system-language-code");
        }
        String systemLanguageCode = properties.systemLanguageCode();
        if (!hasText(properties.deviceModel())) {
            throw new TelegramClientConfigurationException("Model of the device the application is being run on; must be non-empty. " +
                    "Specify property spring.telegram.client.device-model");
        }
        String deviceModel = properties.deviceModel();
        String systemVersion = checkStringOrEmpty(properties.systemVersion());
        String applicationVersion = "1.8.14";
        boolean enableStorageOptimizer = properties.enableStorageOptimizer();
        boolean ignoreFileNames = properties.ignoreFileNames();
        return new TdApi.SetTdlibParameters(
                useTestDc,
                databaseDirectory,
                filesDirectory,
                databaseEncryptionKey,
                useFileDatabase,
                useChatInfoDatabase,
                useMessageDatabase,
                useSecretChats,
                apiId,
                apiHash,
                systemLanguageCode,
                deviceModel,
                systemVersion,
                applicationVersion,
                enableStorageOptimizer,
                ignoreFileNames
        );
    }

    /**
     * Configure and sends proxy settings for TDLib.
     * Proxies: http, socks5, mtProto
     *
     * @param proxy proxy properties
     */
    private void addProxy(TelegramProperties.Proxy proxy) {
        TdApi.ProxyType proxyType = null;
        TelegramProperties.Proxy.ProxyHttp http = proxy.http();
        if (http != null) {
            if (!hasText(http.password()) || !hasText(http.username())) {
                throw new TelegramClientConfigurationException("""
                            Http proxy settings not filled. Specify properties:
                             spring.telegram.client.proxy.http.username
                             spring.telegram.client.proxy.http.password
                             spring.telegram.client.proxy.http.http-only
                             """);
            }
            proxyType = new TdApi.ProxyTypeHttp(http.username(), http.password(), http.httpOnly());
        }
        TelegramProperties.Proxy.ProxySocks5 socks5 = proxy.socks5();
        if (socks5 != null) {
            if (!hasText(socks5.username()) || !hasText(socks5.password())) {
                throw new TelegramClientConfigurationException("""
                            Socks5 proxy settings not filled. Specify properties:
                             spring.telegram.client.proxy.socks5.username
                             spring.telegram.client.proxy.socks5.password
                             """);
            }
            proxyType = new TdApi.ProxyTypeSocks5(socks5.username(), socks5.password());
        }
        TelegramProperties.Proxy.ProxyMtProto mtproto = proxy.mtproto();
        if (mtproto != null) {
            if (!hasText(mtproto.secret())) {
                throw new TelegramClientConfigurationException("MtProto proxy settings not filled. " +
                        "Specify property spring.telegram.client.proxy.mtproto.secret");
            }
            proxyType = new TdApi.ProxyTypeMtproto(mtproto.secret());
        }
        if (!hasText(proxy.server()) || proxy.port() <= 0) {
            throw new TelegramClientConfigurationException("""
                        Proxy settings not filled. Specify properties:
                         spring.telegram.client.proxy.server
                         spring.telegram.client.proxy.port
                        """);
        }
        var addProxy = new TdApi.AddProxy(proxy.server(), proxy.port(), true, proxyType);
        telegramClient.sendSync(addProxy);
    }

    private String checkStringOrEmpty(String s) {
        return hasText(s) ? s : "";
    }

    private class AuthorizationRequestHandler implements Client.ResultHandler {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    log.error("Receive an error:\n" + object);
                    handleNotification(null); // repeat last action
                }
                // result is already received through UpdateAuthorizationState, nothing to do
                case TdApi.Ok.CONSTRUCTOR -> {}
                default -> log.error("Receive wrong response from TDLib:\n" + object);
            }
        }
    }

}
