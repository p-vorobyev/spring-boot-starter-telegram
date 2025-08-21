#!/bin/sh

# Download and install Microsoft Visual Studio. Enable C++ support while installing.
# Download and install CMake; choose "Add CMake to the system PATH" option while installing.
# Download and install Git.
# Download and unpack PHP. Add the path to php.exe to the PATH environment variable.
# Download and install JDK.

echo "Starting build process..."

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 51743dfd01dff6179e2d8f7095729caa4e2222e9
git clone https://github.com/Microsoft/vcpkg.git
cd vcpkg
git checkout bc3512a509f9d29b37346a7e7e929f9a26e66c7e
./bootstrap-vcpkg.bat
./vcpkg.exe install gperf:x64-windows openssl:x64-windows zlib:x64-windows
cd ..
rm -rf build
mkdir build
cd build
cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../vcpkg/scripts/buildsystems/vcpkg.cmake ..
echo "Building and installing core TDLib..."
cmake --build . --target install --config Release
cd ..
cd example/java
rm -rf build
mkdir build
cd build
cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../../../vcpkg/scripts/buildsystems/vcpkg.cmake -DTd_DIR:PATH=$(readlink -e ../td/lib/cmake/Td) ..
echo "Building and installing Java JNI library..."
cmake --build . --target install --config Release
cd ../../..
rm ../../windows_x64/*.dll
cp tdlib/bin/*.dll ../../windows_x64
echo "Library saved to project directory: libs/windows_x64"
echo "Cleaning up temporary files..."
cd ..
rm -rf td
echo "Build process completed successfully!"
