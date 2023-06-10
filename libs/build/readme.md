## Build native library

* To build TDLib you must follow official [instructions](https://tdlib.github.io/td/build.html?language=Java).
* Commit hash for v1.8.13 in TDLib repository - `eb664b4e`.
* After step `cd example/java` 
replace original `CMakeLists.txt` to [CMakeLists.txt](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/CMakeLists.txt) 
and copy directory [dev](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/dev). 
* Continue steps from the official instructions.


We do this because of JNI bindings with custom package structure - `dev.voroby.springframework.telegram.client`. 
