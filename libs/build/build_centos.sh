#!/bin/sh

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 63be0bc9fd942862aa8e585b22ea00a906ff8d19
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DTd_DIR:PATH=$(readlink -e ../td/lib/cmake/Td) ..
cmake --build . --target install
cd ../../..

if [ $(uname -a | grep -c 'aarch64') -eq 1 ]; then
  rm ../../linux_arm64/libtdjni.so
  cp tdlib/bin/libtdjni.so ../../linux_arm64
  echo "Library saved to project directory: libs/linux_arm64"
else
  rm ../../linux_x64/libtdjni.so
  cp tdlib/bin/libtdjni.so ../../linux_x64
  echo "Library saved to project directory: libs/linux_x64"
fi

cd ..
rm -rf td
