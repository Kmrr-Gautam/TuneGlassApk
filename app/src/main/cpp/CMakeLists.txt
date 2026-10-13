cmake_minimum_required(VERSION 3.22.1)
project(listenit_audio LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Support 16 KB page sizes on Android 15+ (e.g. Galaxy S25 series)
add_link_options("-Wl,-z,max-page-size=16384")

find_package(oboe REQUIRED CONFIG)

# libsoxr 0.1.3 is pinned for reproducible HQ sinc resampling.
include(FetchContent)
FetchContent_Declare(
    soxr_source
    URL https://github.com/chirlu/soxr/archive/refs/tags/0.1.3.tar.gz
    URL_HASH SHA256=db6ca1b1e8405c6ef92f8294fc123d910abf0a114003b3f0f13fa57a95fd62d0
)
FetchContent_GetProperties(soxr_source)
if(NOT soxr_source_POPULATED)
    FetchContent_Populate(soxr_source)
endif()

add_library(
    soxr
    SHARED
    "${soxr_source_SOURCE_DIR}/src/soxr.c"
    "${soxr_source_SOURCE_DIR}/src/data-io.c"
    "${soxr_source_SOURCE_DIR}/src/dbesi0.c"
    "${soxr_source_SOURCE_DIR}/src/filter.c"
    "${soxr_source_SOURCE_DIR}/src/fft4g64.c"
    "${soxr_source_SOURCE_DIR}/src/fft4g32.c"
    "${soxr_source_SOURCE_DIR}/src/cr.c"
    "${soxr_source_SOURCE_DIR}/src/cr32.c"
    "${soxr_source_SOURCE_DIR}/src/cr64.c"
    "${soxr_source_SOURCE_DIR}/src/vr32.c"
)
target_compile_definitions(soxr PRIVATE -DSOXR_LIB)
target_include_directories(soxr PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}" "${soxr_source_SOURCE_DIR}/src")
target_link_libraries(soxr PRIVATE m)
set_target_properties(soxr PROPERTIES VERSION "" SOVERSION "")

add_library(
    listenit_audio
    SHARED
    AudioEngine.cpp
    DspProcessor.cpp
    NativeBridge.cpp
)

target_include_directories(
    listenit_audio
    PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}"
    "${soxr_source_SOURCE_DIR}/src"
)

target_compile_options(
    listenit_audio
    PRIVATE
    -Wall
    -Wextra
    -Wpedantic
    -Wconversion
    -Wshadow
)

target_link_libraries(
    listenit_audio
    PRIVATE
    oboe::oboe
    soxr
    android
    log
)
