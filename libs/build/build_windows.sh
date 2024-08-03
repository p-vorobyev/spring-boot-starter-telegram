#!/bin/sh

# Download and install Microsoft Visual Studio. Enable C++ support while installing.
# Download and install CMake; choose "Add CMake to the system PATH" option while installing.
# Download and install Git.
# Download and unpack PHP. Add the path to php.exe to the PATH environment variable.
# Download and install JDK.


rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout a24af099
git clone https://github.com/Microsoft/vcpkg.git
cd vcpkg
git checkout cd5e746ec203c8c3c61647e0886a8df8c1e78e41
./bootstrap-vcpkg.bat
./vcpkg.exe install gperf:x64-windows openssl:x64-windows zlib:x64-windows
cd ..
rm -rf build
mkdir build
cd build
cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../vcpkg/scripts/buildsystems/vcpkg.cmake ..
cmake --build . --target install --config Release
cd ..
rm example/java/CMakeLists.txt
cp ../CMakeLists.txt example/java
cp -R ../dev example/java
cd example/java
rm -rf build
mkdir build
cd build
cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../../../vcpkg/scripts/buildsystems/vcpkg.cmake -DTd_DIR:PATH=$(readlink -e ../td/lib/cmake/Td) ..
cmake --build . --target install --config Release
cd ../../..
rm ../../windows_x64/*.dll
cp tdlib/bin/*.dll ../../windows_x64
echo "Library saved to project directory: libs/windows_x64"
cd ..
rm -rf td