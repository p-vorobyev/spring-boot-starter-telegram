# Telegram. TDLib Spring Boot Starter

Spring Boot Starter for [Telegram](https://telegram.org) based on [TDLib](https://github.com/tdlib/td).

![](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/img/logo.png)

## Contents
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration properties](#configuration)
- [Example](#example)
- [Desktop client example](https://github.com/p-vorobyev/telegram-desktop-compose-multiplatform)
- [Templates](#templates)
- [Runners](#runners)
- [Javadoc](https://p-vorobyev.github.io/javadoc_spring-boot-starter-telegram)
- [Notice](#notice)
- [License](#license)
- [Acknowledgements](#acknowledgements)

<a name="requirements"></a>
## Requirements
| Technology  | Version  |
|-------------|----------|
| jdk         | 17       |
| TDLib       | 1.8.37   |
| Spring Boot | 3        |

TDLib [depends](https://github.com/tdlib/td#dependencies) on:

- C++14 compatible compiler (Clang 3.4+, GCC 4.9+, MSVC 19.0+ (Visual Studio 2015+), Intel C++ Compiler 17+)
- OpenSSL
- zlib

<a name="installation"></a>
## Installation
1) Download by one of two options:
 - 1.1 Clone source code:
```shell
git clone https://github.com/p-vorobyev/spring-boot-starter-telegram.git
```

&nbsp;&nbsp;&nbsp;&nbsp;or

 - 1.2 Download artifact from GitHub Packages:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Maven**:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Specify `github` server with your credentials in `settings.xml` for Apache Maven. See GitHub [docs](https://docs.github.com/ru/enterprise-cloud@latest/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) how to generate personal token.

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <servers>
        <server>
            <id>github</id>
            <username>GITHUB_LOGIN</username>
            <password>GITHUB_TOKEN</password>
        </server>
    </servers>

</settings>
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Add repository to `pom.xml` of your project.

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/p-vorobyev/*</url>
    </repository>
</repositories>
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Gradle**:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Specify repository in `build.gradle.kts` with your GitHub login and personal token. 

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/p-vorobyev/*")
        credentials {
            username = "GITHUB_LOGIN"
            password = "GITHUB_TOKEN"
        }
    }
}
```

2) Create your Spring Boot module.

3) Add dependency to your project:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Maven**:

```xml
<!-- Java -->
<dependency>
    <groupId>dev.voroby</groupId>
    <artifactId>spring-boot-starter-telegram</artifactId>
    <version>1.14.0</version>
</dependency>

<!-- Kotlin -->
<dependency>
    <groupId>dev.voroby</groupId>
    <artifactId>spring-boot-starter-telegram-kt</artifactId>
    <version>1.14.0</version>
</dependency>
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Gradle**:

```kotlin
// Java
implementation("dev.voroby:spring-boot-starter-telegram:1.14.0")

// Kotlin
implementation("dev.voroby:spring-boot-starter-telegram-kt:1.14.0")
```

Or just download artifact by path `Releases -> 1.14.0 -> dev.voroby.spring-boot-starter-telegram (maven) -> Assets -> spring-boot-starter-telegram-1.14.0.jar` 
from the latest release and add it to your project's classpath instead of the steps above.

4) Specify JVM property for compiled TDLib shared library path:
```shell
-Djava.library.path=<path_to_shared_library>
```
You can find compiled libraries for several platforms in the `libs.zip` archive from the latest [release](https://github.com/p-vorobyev/spring-boot-starter-telegram/releases/tag/1.14.0).
If you haven't found a library for your OS and architecture, you can build it yourself following these [instructions](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/readme.md).

5) If you are using IntelliJ IDEA, set the property `idea.max.intellisense.filesize` for comfortable work with `TdApi` 
objects. Go to `Help -> Edit Custom Properties...` and add `idea.max.intellisense.filesize=5000`, then restart the IDE. 
([Info](https://github.com/tdlib/td/issues/2949))

<a name="configuration"></a>
## Configuration properties

Mandatory properties for autoconfiguration:

| property                                          | type   | description                                                                                                      |
|---------------------------------------------------|--------|------------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.api-id`                   | int    | Application identifier for Telegram API access, which can be obtained at https://my.telegram.org                 |
| `spring.telegram.client.api-hash`                 | String | Application identifier hash for Telegram API access, which can be obtained at https://my.telegram.org            |
| `spring.telegram.client.phone`                    | String | The phone number of the user, in international format.                                                           |
| `spring.telegram.client.database-encryption-key`  | String | Encryption key for the database. If the encryption key is invalid, then an error with code 401 will be returned. |
| `spring.telegram.client.system-language-code`  | String | IETF language tag of the user's operating system language; must be non-empty.                                    |
| `spring.telegram.client.device-model`  | String | Model of the device the application is being run on; must be non-empty.                                          |

Additional properties:

| property                                          | type    | description                                                                                                                                                |
|---------------------------------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.database-directory`                   | String  | The path to the directory for the persistent database; if empty, the current working directory will be used.                                               |
| `spring.telegram.client.use-file-database`                   | boolean | Pass true to keep information about downloaded and uploaded files between application restarts.                                                            |
| `spring.telegram.client.use-chat-info-database`                   | boolean | Pass true to keep cache of users, basic groups, supergroups, channels and secret chats between restarts. Implies `use-file-database`.                      |
| `spring.telegram.client.use-message-database`                   | boolean | Pass true to keep cache of chats and messages between restarts. Implies `use-chat-info-database`.                                                          |
| `spring.telegram.client.use-secret-chats`                   | boolean | Pass true to enable support for secret chats.                                                                                                              |
| `spring.telegram.client.log-verbosity-level`                   | int     | The maximum verbosity level of messages for which the callback will be called.                                                                             |
| `spring.telegram.client.system-version`                   | String  | Version of the operating system the application is being run on. If empty, the version is automatically detected by TDLib.                                 |
| `spring.telegram.client.use-test-dc`                   | boolean | Pass true to use Telegram test environment instead of the production environment. |

Using proxy(Http/Socks5/MtProto):

| property                                          | type   | description                                                                                                   |
|---------------------------------------------------|--------|---------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.proxy.server`                   | String | Proxy server address.  |
| `spring.telegram.client.proxy.port`                   | int    | Proxy port.  |

- Http

| property                                          | type    | description                                                                                                  |
|---------------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.proxy.http.username`                   | String  | Http proxy username. |
| `spring.telegram.client.proxy.http.password`                   | String  | Http proxy password. |
| `spring.telegram.client.proxy.http.http-only`                   | boolean | Pass true if the proxy supports only HTTP requests and doesn't support transparent TCP connections via HTTP CONNECT method. |

- Socks5

| property                                          | type    | description                                                                                                  |
|---------------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.proxy.socks5.username`                   | String  | Socks5 proxy username. |
| `spring.telegram.client.proxy.socks5.password`                   | String  | Socks5 proxy password. |

- MtProto

| property                                          | type    | description                                                                                                  |
|---------------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------|
| `spring.telegram.client.proxy.mtproto.secret`                   | String  | MtProto proxy secret. |

<a name="example"></a>
## Example
1) Specify `application.properties`:
```properties
spring.telegram.client.api-id=${TELEGRAM_API_ID}
spring.telegram.client.api-hash=${TELEGRAM_API_HASH}
spring.telegram.client.phone=${TELEGRAM_API_PHONE}
spring.telegram.client.database-encryption-key=${TELEGRAM_API_DATABASE_ENCRYPTION}
spring.telegram.client.device-model=my_telegram
spring.telegram.client.use-message-database=true
spring.telegram.client.use-file-database=true
spring.telegram.client.use-chat-info-database=true
spring.telegram.client.use-secret-chats=true
spring.telegram.client.log-verbosity-level=1
spring.telegram.client.database-directory=/my/directory/database
```

2) Now we can inject and work with client and authorization beans.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Java**:

```java
@Autowired
private TelegramClient telegramClient;

@Autowired
private ClientAuthorizationState authorizationState;
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Kotlin**:

```kotlin
@Autowired
lateinit var kTelegramClient: KTelegramClient
```

3) At the first start you need to authorize client. If the client waiting for some credentials you can check this
 in several ways:
- `authorizationState` api

```java
/**
 * @return authentication sate awaiting authentication code
 */
boolean isWaitAuthenticationCode();

/**
 * @return authentication sate awaiting two-step verification password
 */
boolean isWaitAuthenticationPassword();

/**
 * @return authentication sate awaiting email address
 */
boolean isWaitEmailAddress();

/**
 * @return authorization status
 */
boolean haveAuthorization();

/**
 * All databases are closed and all resources are released. No other updates will be received after this.
 * All queries will be responded to with error code 500.
 * @return is TDLib client in its final state
 */
boolean isStateClosed();
```
- application log 
```text
INFO 10647 --- [   TDLib thread] .s.t.c.u.UpdateAuthorizationNotification : Please enter authentication code
```
```text
INFO 10647 --- [   TDLib thread] .s.t.c.u.UpdateAuthorizationNotification : Please enter password
```

You can check and send credentials with `authorizationState`. TDLib will save the session in the database. All functionality is now available.
The next time you run the client, it will use the saved session until you clear the database or terminate the session from another
client(official app, etc.).

4) **TDLib query usage examples**:

- An example of synchronous call. Let's get `TdApi.Chat` object by id:
```java
TdApi.Chat chat = telegramClient.sendSync(new TdApi.GetChat(chatId));
```

- An example of asynchronous call with callback. Let's get info about ourselves:
```java
telegramClient.sendWithCallback(new TdApi.GetMe(), (user, error) -> {
    if (error != null) {
        log.error("Error: [code: {}, message: {}]", error.code, error.message);
    } else {
        Optional.ofNullable(user.usernames)
            .ifPresent(usernames -> log.info("Active username: {}", usernames.activeUsernames[0]));
    }
});
```

- An example of asynchronous call with `CompletableFuture`. Let's send hello message to ourselves:
```java
telegramClient.sendAsync(new TdApi.GetMe())
        .thenApply(user -> user.object().usernames.activeUsernames[0])
        .thenApply(username -> telegramClient.sendAsync(new TdApi.SearchChats(username, 1)))
        .thenCompose(chatsFuture ->
        chatsFuture.thenApply(chats -> chats.object().chatIds[0]))
        .thenApply(chatId -> telegramClient.sendAsync(sendMessageQuery(chatId)));

private TdApi.SendMessage sendMessageQuery(Long chatId) {
        var content = new TdApi.InputMessageText();
        var formattedText = new TdApi.FormattedText();
        formattedText.text = "Hello!";
        content.text = formattedText;
        return new TdApi.SendMessage(chatId, 0, null, null, null, content);
    }
```

5) **Register implementations of `UpdateNotificationListener` and handle updates from TDLib.** For example, let's 
listen an incoming messages notification: 

```java
@Component @Slf4j
public class UpdateNewMessageHandler implements UpdateNotificationListener<TdApi.UpdateNewMessage> {

    @Override
    public void handleNotification(TdApi.UpdateNewMessage notification) {
        TdApi.Message message = notification.message;
        TdApi.MessageContent content = message.content;
        if (content instanceof TdApi.MessageText mt) {
            log.info("Incoming text message:\n[\n\tchatId: {},\n\tmessage: {}\n]", 
                    message.chatId, mt.text.text);
        }
    }

    @Override
    public Class<TdApi.UpdateNewMessage> notificationType() {
        return TdApi.UpdateNewMessage.class;
    }

}
```

You can find usage example in [simple-client](https://github.com/p-vorobyev/spring-boot-starter-telegram/tree/master/simple-client) app.

<a name="templates"></a>
## Templates
Templates simplify the use of `TelegramClient` for related objects. Just autowire them and use:

- [UserTemplate](https://p-vorobyev.github.io/javadoc_spring-boot-starter-telegram/dev/voroby/springframework/telegram/client/templates/UserTemplate.html)
- [ChatTemplate](https://p-vorobyev.github.io/javadoc_spring-boot-starter-telegram/dev/voroby/springframework/telegram/client/templates/ChatTemplate.html)

<a name="runners"></a>
## Runners
You can register implementations of the `TelegramRunner` interface to run it when application starts. It will be executed 
only after authorization state is ready and TDLib is ready to answer general requests. Multiple `TelegramRunner` beans 
can be defined within the same application context and can be ordered using `@Order` annotation. You can see an [example](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/simple-client/src/main/java/dev/voroby/springframework/telegram/ChatsLoader.java) 
how to load chats when application starts.

<a name="notice"></a>
## Notice
Be careful and do not push personal data like `api-id`,`api-hash`, `phone` to remote repositories.

<a name="license"></a>
## License
[MIT License](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/LICENSE)

<a name="acknowledgements"></a>
## Acknowledgements
Thanks for the best IDE [support](https://jb.gg/OpenSourceSupport).

<img alt="jb_beam" src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="400"/>
