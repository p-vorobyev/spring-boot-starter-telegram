## Build native library

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Build by script**:

* Go to the project directory `cd libs/build`.
* Prepare environment for build by script `./prepare_env_<your_os_name>.sh`(skip this step for Windows).
* Run build script `./build_<your_os_name>.sh`(for Windows, you can find instructions for preparing the environment 
in the `build_windows.sh` file comments).

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Build manually**:

* To build TDLib you must follow official [instructions](https://tdlib.github.io/td/build.html?language=Java).
* After step `git clone https://github.com/tdlib/td.git` checkout to v1.8.34 in TDLib repository `git checkout a24af099`.
* Continue steps from the official instructions.
* After step `cd example/java` 
replace original `CMakeLists.txt` to [CMakeLists.txt](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/CMakeLists.txt) 
and copy directory [dev](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/libs/build/dev): 

![](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/img/custome_package_client.png)

* Continue steps from the official instructions.
* Then you can use your own shared library:

![](https://github.com/p-vorobyev/spring-boot-starter-telegram/blob/master/img/tdjni.png)


We do these steps because of JNI bindings with custom package structure - `dev.voroby.springframework.telegram.client`. 
