# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
project(FreightFrenzy)

cmake_minimum_required(VERSION 3.19.0)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             TeamCode

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native_pipeline.cpp)

# Specifies a path to native header files.
include_directories(src/main/cpp/include/
                    ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniIncludes/)

# Specify path to prebuilt object
add_library(openCvNative SHARED IMPORTED)
set_target_properties(openCvNative PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibraries/${ANDROID_ABI}/libopencv_java4.so)

# linking libs
target_link_libraries(
        TeamCode
        openCvNative)