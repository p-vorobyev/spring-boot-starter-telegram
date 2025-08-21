#!/bin/zsh

echo "Starting build process..."

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 51743dfd01dff6179e2d8f7095729caa4e2222e9
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DJAVA_HOME=$JAVA_HOME -DOPENSSL_ROOT_DIR=/opt/homebrew/opt/openssl/ -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..
echo "Building and installing core TDLib..."
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DJAVA_HOME=$JAVA_HOME -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DTd_DIR:PATH=$(greadlink -e ../td/lib/cmake/Td) ..
echo "Building and installing Java JNI library..."
cmake --build . --target install
cd ../../..

if [ $(uname -a | grep -c 'arm64') -eq 1 ]; then
  echo "Detected ARM64 architecture."
  rm ../../macos_silicon/libtdjni.dylib
  cp tdlib/bin/libtdjni.dylib ../../macos_silicon
  echo "Library saved to project directory: libs/macos_silicon"
else
  echo "Detected x64 architecture."
  rm ../../macos_x64/libtdjni.dylib
  cp tdlib/bin/libtdjni.dylib ../../macos_x64
  echo "Library saved to project directory: libs/macos_x64"
fi

echo "Cleaning up temporary files..."
cd ..
rm -rf td
echo "Build process completed successfully!"
