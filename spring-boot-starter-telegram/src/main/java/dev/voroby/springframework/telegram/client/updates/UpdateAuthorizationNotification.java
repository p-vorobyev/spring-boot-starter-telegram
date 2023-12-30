package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.client.QueryResultHandler;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.exception.TelegramClientConfigurationException;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.StringUtils.hasText;

/**
 * Handler of {@link TdApi.AuthorizationState} updates.
 */
public class UpdateAuthorizationNotification implements UpdateNotificationListener<TdApi.UpdateAuthorizationState> {

    private final Logger log = LoggerFactory.getLogger(UpdateAuthorizationNotification.class);

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
    public void handleNotification(TdApi.UpdateAuthorizationState notification) {
        if (notification != null) {
            TdApi.AuthorizationState newAuthorizationState = notification.authorizationState;
            if (newAuthorizationState != null) {
                this.authorizationState = newAuthorizationState;
            }
        }
        switch (this.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                TdApi.SetTdlibParameters tdLibParameters = tdLibParameters();
                log.info("TDLib version: " + tdLibParameters.applicationVersion);
                telegramClient.sendWithCallback(tdLibParameters, new AuthorizationRequestHandler());
                TelegramProperties.Proxy proxy = properties.proxy();
                if (proxy != null) {
                    addProxy(proxy);
                }
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR ->
                    telegramClient.sendWithCallback(new TdApi.SetAuthenticationPhoneNumber(properties.phone(), null), new AuthorizationRequestHandler());
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
                            awaitInput();
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
                            awaitInput();
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
                            awaitInput();
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
                            awaitInput();
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
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                clientAuthorizationState.setStateClosed();
                log.info("Closed");
            }
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

    private void awaitInput() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Configure TDLib parameters.
     * @return {@link TdApi.SetTdlibParameters}
     */
    private TdApi.SetTdlibParameters tdLibParameters() {
        boolean useTestDc = properties.useTestDc();
        String databaseDirectory = checkStringOrEmpty(properties.databaseDirectory());
        String filesDirectory = checkStringOrEmpty(properties.filesDirectory());
        byte[] databaseEncryptionKey = properties.databaseEncryptionKey().getBytes(StandardCharsets.UTF_8);
        boolean useFileDatabase = properties.useFileDatabase();
        boolean useChatInfoDatabase = properties.useChatInfoDatabase();
        boolean useMessageDatabase = properties.useMessageDatabase();
        boolean useSecretChats = properties.useSecretChats();
        int apiId = properties.apiId();
        String apiHash = properties.apiHash();
        String systemLanguageCode = properties.systemLanguageCode();
        String deviceModel = properties.deviceModel();
        String systemVersion = checkStringOrEmpty(properties.systemVersion());
        String applicationVersion = "1.8.23";
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
        TelegramProperties.Proxy.ProxyHttp http = proxy.http();
        TelegramProperties.Proxy.ProxySocks5 socks5 = proxy.socks5();
        TelegramProperties.Proxy.ProxyMtProto mtProto = proxy.mtproto();
        TdApi.ProxyType proxyType;
        if (http != null) {
            proxyType = new TdApi.ProxyTypeHttp(http.username(), http.password(), http.httpOnly());
        } else if (socks5 != null) {
            proxyType = new TdApi.ProxyTypeSocks5(socks5.username(), socks5.password());
        } else if (mtProto != null) {
            proxyType = new TdApi.ProxyTypeMtproto(mtProto.secret());
        } else {
            throw new TelegramClientConfigurationException("ProxyType not filled. Available types - http, socks5, mtProto");
        }
        var addProxy = new TdApi.AddProxy(proxy.server(), proxy.port(), true, proxyType);
        telegramClient.sendSync(addProxy);
    }

    private String checkStringOrEmpty(String s) {
        return hasText(s) ? s : "";
    }

    private class AuthorizationRequestHandler implements QueryResultHandler<TdApi.Ok> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onResult(TdApi.Ok obj, TdApi.Error error) {
            if (error != null) {
                log.error("Receive an error:\n" + error);
                handleNotification(null); // repeat last action
            }
            //result is already received through UpdateAuthorizationState, nothing to do
        }
    }

}
