# TDLib Telegram client with Spring Boot

This is a basic example of how to get started with TDLib. In `libs` directory you can find shared library of TDLib 
v1.8.0 for macos and centos. Follow instructions on TDLib page https://tdlib.github.io/td/build.html?language=Java 
to compile shared library for your operating system(or another version).

### Configure variables

`DATABASE_DIR` - directory for mobile database of the client

`API_ID` - your telegram api key

`API_HASH` - your telegram api hash

`PHONE_NUMBER` -  your account phone number

`ENABLE_MT_PROTO` - only if you want to use MTProto(optional)

`MT_PROTO_SECRET` - secret for MTProxy (optional)

`PROXY_HOST` - (optional)

`PROXY_PORT` - (optional)

### Build and run 🚀
Check your java version. This example uses java 17 👌

```shell
mvn verify
java jar target/telegram_client-0.0.1-SNAPSHOT.jar -Djava.library.path=<path_to_directory_with_tdlib_shared_library>
```
