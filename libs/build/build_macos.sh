#!/bin/zsh

echo "Starting build process..."

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 022d60202e446ad1287b9fb68e687c8a0760788b
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release \
      -DJAVA_HOME="$JAVA_HOME" \
      -DOPENSSL_ROOT_DIR=/opt/homebrew/opt/openssl/ \
      -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td \
      -DTD_ENABLE_JNI=ON ..
echo "Building and installing core TDLib..."
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release \
      -DJAVA_HOME="$JAVA_HOME" \
      -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib \
      -DTd_DIR:PATH=$(greadlink -e ../td/lib/cmake/Td) ..
echo "Building and installing Java JNI library..."
cmake --build . --target install
cd ../../..

# Determine architecture
case $(uname -m) in
  arm64)
    DEST_DIR="../../macos_silicon"
    ;;
  x86_64)
    DEST_DIR="../../macos_x64"
    ;;
  *)
    echo "Unsupported architecture: $(uname -m)" >&2
    exit 1
    ;;
esac

cp tdlib/bin/libtdjni.dylib $DEST_DIR
echo "Library saved to project directory: $DEST_DIR"
# Cleanup on exit
rm -rf td
echo "Build process completed successfully!"
