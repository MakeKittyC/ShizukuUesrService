cmake_minimum_required(VERSION 3.22.1)

project(TerminateProcess)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(native-lib
    ${log}
    ${android}
)

target_link_options(native-lib PRIVATE "-Wl,-z,max-page-size=16384")

# 4096 16384 65536