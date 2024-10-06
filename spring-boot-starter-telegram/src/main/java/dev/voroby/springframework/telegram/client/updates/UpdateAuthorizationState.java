package dev.voroby.springframework.telegram.client.updates;

import dev.voroby.springframework.telegram.client.QueryResultHandler;
import org.drinkless.tdlib.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.exception.TelegramClientConfigurationException;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static dev.voroby.springframework.telegram.client.updates.AuthorizationStateCache.*;
import static org.springframework.util.StringUtils.hasText;

/**
 * Handler of {@link TdApi.AuthorizationState} updates.
 */
public class UpdateAuthorizationState implements UpdateNotificationListener<TdApi.UpdateAuthorizationState> {

    private final Logger log = LoggerFactory.getLogger(UpdateAuthorizationState.class);

    private TdApi.AuthorizationState authorizationState;

    private final TelegramProperties properties;

    private final TelegramClient telegramClient;

    private final AuthorizationRequestHandler authorizationRequestHandler;

    public UpdateAuthorizationState(TelegramProperties properties,
                                    TelegramClient telegramClient) {
        this.properties = properties;
        this.telegramClient = telegramClient;
        this.authorizationRequestHandler = new AuthorizationRequestHandler();
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
                log.info("TDLib version: {}", tdLibParameters.applicationVersion);
                telegramClient.sendWithCallback(tdLibParameters, authorizationRequestHandler);
                TelegramProperties.Proxy proxy = properties.proxy();
                if (proxy != null) {
                    addProxy(proxy);
                }
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                var setAuthenticationPhoneNumber = new TdApi.SetAuthenticationPhoneNumber(properties.phone(), null);
                telegramClient.sendWithCallback(setAuthenticationPhoneNumber, authorizationRequestHandler);
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
                log.info("Please confirm this login link on another device: {}", link);
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                try {
                    if (!hasText(codeInputToCheck)) {
                        waitAuthenticationCode.set(true);
                        while (!hasText(codeInputToCheck)) {
                            log.info("Please enter authentication code");
                            awaitInput();
                        }
                    }
                    var checkAuthenticationCode = new TdApi.CheckAuthenticationCode(codeInputToCheck);
                    telegramClient.sendWithCallback(checkAuthenticationCode, authorizationRequestHandler);
                } finally {
                    codeInputToCheck = null;
                }
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                try {
                    if (!hasText(passwordInputToCheck)) {
                        waitAuthenticationPassword.set(true);
                        while (!hasText(passwordInputToCheck)) {
                            log.info("Please enter password");
                            awaitInput();
                        }
                    }
                    var checkAuthenticationPassword = new TdApi.CheckAuthenticationPassword(passwordInputToCheck);
                    telegramClient.sendWithCallback(checkAuthenticationPassword, authorizationRequestHandler);
                } finally {
                    passwordInputToCheck = null;
                }
            }
            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
                try {
                    if (!hasText(emailAddressInputToCheck)) {
                        waitEmailAddress.set(true);
                        while (!hasText(emailAddressInputToCheck)) {
                            log.info("Please enter email");
                            awaitInput();
                        }
                    }
                    var setAuthenticationEmailAddress = new TdApi.SetAuthenticationEmailAddress(emailAddressInputToCheck);
                    telegramClient.sendWithCallback(setAuthenticationEmailAddress, authorizationRequestHandler);
                } finally {
                    emailAddressInputToCheck = null;
                }
            }
            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
                try {
                    if (!hasText(codeInputToCheck)) {
                        waitAuthenticationCode.set(true);
                        while (!hasText(codeInputToCheck)) {
                            log.info("Please enter authentication code from email");
                            awaitInput();
                        }
                    }
                    var emailAuth = new TdApi.EmailAddressAuthenticationCode(codeInputToCheck);
                    var checkAuthenticationEmailCode = new TdApi.CheckAuthenticationEmailCode(emailAuth);
                    telegramClient.sendWithCallback(checkAuthenticationEmailCode, authorizationRequestHandler);
                } finally {
                    codeInputToCheck = null;
                }
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> haveAuthorization.set(true);
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                haveAuthorization.set(false);
                log.info("Logging out");
            }
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                haveAuthorization.set(false);
                log.info("Closing");
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                stateClosed.set(true);
                log.info("Closed");
            }
            default -> log.error("Unsupported authorization state:\n{}", this.authorizationState);
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
        String applicationVersion = "1.8.37";
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
                applicationVersion
        );
    }

    /**
     * Configure and sends proxy settings for TDLib.
     * Proxies: http, socks5, mtProto
     *
     * @param proxy proxy properties
     */
    private void addProxy(TelegramProperties.Proxy proxy) {
        TdApi.ProxyType proxyType = getProxyType(proxy);
        var addProxy = new TdApi.AddProxy(proxy.server(), proxy.port(), true, proxyType);
        telegramClient.sendWithCallback(addProxy, ((obj, error) -> {
            if (error == null) {
                log.info("Proxy server: [server: {}, port: {}, type: {}]",
                        addProxy.server, addProxy.port, proxyType.getClass().getSimpleName());
            }
        }));
    }

    private static TdApi.ProxyType getProxyType(TelegramProperties.Proxy proxy) {
        var http = proxy.http();
        var socks5 = proxy.socks5();
        var mtProto = proxy.mtproto();
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
        return proxyType;
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
                log.error("Receive an error:\n{}", error);
                handleNotification(null); // repeat last action
            }
            //result is already received through UpdateAuthorizationState, nothing to do
        }
    }

}
