package dev.voroby.springframework.telegram.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Telegram client configuration properties.
 *
 * @author Pavel Vorobyev
 */
@ConfigurationProperties(prefix = "spring.telegram.client")
public record TelegramProperties(
        boolean useTestDc,
        String databaseDirectory,
        String filesDirectory,
        String databaseEncryptionKey,
        boolean useFileDatabase,
        boolean useChatInfoDatabase,
        boolean useMessageDatabase,
        boolean useSecretChats,
        int apiId,
        String apiHash,
        String phone,
        String systemLanguageCode,
        String deviceModel,
        String systemVersion,
        String applicationVersion,
        boolean enableStorageOptimizer,
        boolean ignoreFileNames,
        int logVerbosityLevel,
        Proxy proxy
) {

    public record Proxy(
            String server,
            int port,
            ProxyHttp http,
            ProxySocks5 socks5,
            ProxyMtProto mtproto
    ) {
        public record ProxyHttp(String username, String password, boolean httpOnly) {}

        public record ProxySocks5(String username, String password) {}

        public record ProxyMtProto(String secret) {}
    }

}
