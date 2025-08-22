#!/bin/sh

echo "Starting build process..."

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 5c77c4692c28eb48a68ef1c1eeb1b1d732d507d3
rm -rf build
mkdir build
cd build
CXXFLAGS="-stdlib=libc++" CC=/usr/bin/clang-18 CXX=/usr/bin/clang++-18 cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..
echo "Building and installing core TDLib..."
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
CXXFLAGS="-stdlib=libc++" CC=/usr/bin/clang-18 CXX=/usr/bin/clang++-18 cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DTd_DIR:PATH=$(readlink -e ../td/lib/cmake/Td) ..
echo "Building and installing Java JNI library..."
cmake --build . --target install
cd ../../..

if [ $(uname -a | grep -c 'aarch64') -eq 1 ]; then
  echo "Detected ARM64 architecture."
  rm ../../ubuntu24_arm64/libtdjni.so
  cp tdlib/bin/libtdjni.so ../../ubuntu24_arm64
  echo "Library saved to project directory: libs/ubuntu24_arm64"
else
  echo "Detected x64 architecture."
  rm ../../ubuntu24_x64/libtdjni.so
  cp tdlib/bin/libtdjni.so ../../ubuntu24_x64
  echo "Library saved to project directory: libs/ubuntu24_x64"
fi

echo "Cleaning up temporary files..."
cd ..
rm -rf td
echo "Build process completed successfully!"
