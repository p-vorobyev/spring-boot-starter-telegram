## Build native library

* To build TDLib you must follow official [instructions](https://tdlib.github.io/td/build.html?language=Java).
* Commit hash for v1.8.20 in TDLib repository - `4ed0b23c`.
* After step `cd example/java` 
replace original `CMakeLists.txt` to [CMakeLists.txt](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/CMakeLists.txt) 
and copy directory [dev](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/dev): 

![](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/img/custome_package_client.png)

* Continue steps from the official instructions.
* Then you can use your own shared library:

![](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/img/tdjni.png)


We do these steps because of JNI bindings with custom package structure - `dev.voroby.springframework.telegram.client`. 
