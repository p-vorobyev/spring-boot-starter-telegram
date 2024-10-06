#!/bin/zsh

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 63be0bc9fd942862aa8e585b22ea00a906ff8d19
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DJAVA_HOME=$JAVA_HOME -DOPENSSL_ROOT_DIR=/opt/homebrew/opt/openssl/ -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DJAVA_HOME=$JAVA_HOME -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DTd_DIR:PATH=$(greadlink -e ../td/lib/cmake/Td) ..
cmake --build . --target install
cd ../../..

if [ $(uname -a | grep -c 'arm64') -eq 1 ]; then
  rm ../../macos_silicon/libtdjni.dylib
  cp tdlib/bin/libtdjni.dylib ../../macos_silicon
  echo "Library saved to project directory: libs/macos_silicon"
else
  rm ../../macos_x64/libtdjni.dylib
  cp tdlib/bin/libtdjni.dylib ../../macos_x64
  echo "Library saved to project directory: libs/macos_x64"
fi

cd ..
rm -rf td
