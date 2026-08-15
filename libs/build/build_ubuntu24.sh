#!/bin/sh

echo "Starting build process..."

rm -rf td
git clone https://github.com/tdlib/td.git
cd td
git checkout 022d60202e446ad1287b9fb68e687c8a0760788b
rm -rf build
mkdir build
cd build
CXXFLAGS="-stdlib=libc++" CC=/usr/bin/clang-18 CXX=/usr/bin/clang++-18 \
cmake -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td \
      -DTD_ENABLE_JNI=ON ..
echo "Building and installing core TDLib..."
cmake --build . --target install
cd ..
cd example/java
rm -rf build
mkdir build
cd build
CXXFLAGS="-stdlib=libc++" CC=/usr/bin/clang-18 CXX=/usr/bin/clang++-18 \
cmake -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib \
      -DTd_DIR:PATH=$(readlink -e ../td/lib/cmake/Td) ..
echo "Building and installing Java JNI library..."
cmake --build . --target install
cd ../../..

# Determine architecture
case $(uname -m) in
  aarch64)
    DEST_DIR="../../ubuntu24_arm64"
    ;;
  x86_64)
    DEST_DIR="../../ubuntu24_x64"
    ;;
  *)
    echo "Unsupported architecture: $(uname -m)" >&2
    exit 1
    ;;
esac

cp tdlib/bin/libtdjni.so $DEST_DIR
echo "Library saved to project directory: $DEST_DIR"
# Cleanup on exit
rm -rf td
echo "Build process completed successfully!"
