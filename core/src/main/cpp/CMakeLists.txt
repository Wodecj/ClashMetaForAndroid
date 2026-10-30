cmake_minimum_required(VERSION 3.0)

# Modified by Wodecj, 2026-10-30: detached/shallow and source-archive builds.
# Read the pinned kernel commit directly; a remote branch need not contain it.
set(KERNEL_SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/../../foss/golang/clash")
set(COMMIT_HASH "source")
set(CURRENT_BRANCH "pinned")
if(EXISTS "${KERNEL_SOURCE}/.git")
  execute_process(
    COMMAND git -C "${KERNEL_SOURCE}" rev-parse --short=7 HEAD
    OUTPUT_VARIABLE KERNEL_COMMIT
    RESULT_VARIABLE KERNEL_GIT_RESULT
    OUTPUT_STRIP_TRAILING_WHITESPACE
  )
  if(KERNEL_GIT_RESULT EQUAL 0 AND NOT "${KERNEL_COMMIT}" STREQUAL "")
    set(COMMIT_HASH "${KERNEL_COMMIT}")
  endif()
endif()
message(STATUS "kernel version = ${CURRENT_BRANCH}_${COMMIT_HASH}")

# 获取生成时间
string(TIMESTAMP COMPILE_TIME "%y%m%d")
string (REGEX REPLACE "[\n\t\r]" "" COMPILE_TIME ${COMPILE_TIME})
string(REGEX REPLACE "\"" "" COMPILE_TIME ${COMPILE_TIME})

# 生成版本信息
set(GIT_VERSION "${CURRENT_BRANCH}_${COMMIT_HASH}_${COMPILE_TIME}")
message(STATUS "version info = ${GIT_VERSION}")

# 去除空格
string(REGEX REPLACE "[ ]+" "" GIT_VERSION "${GIT_VERSION}")

# 保存变量到文件
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/version.h.in ${CMAKE_CURRENT_SOURCE_DIR}/version.h @ONLY)

project(clash-bridge C)


set(CMAKE_POSITION_INDEPENDENT_CODE on)
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O3")

set(GO_OUTPUT_BASE ${GO_OUTPUT}/${FLAVOR_NAME})

if ("${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Debug")
elseif ("${CMAKE_BUILD_TYPE}" STREQUAL "Release")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Release")
elseif ("${CMAKE_BUILD_TYPE}" STREQUAL "RelWithDebInfo")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Release")
else ()
    message(FATAL_ERROR "Unknown build type ${CMAKE_BUILD_TYPE}")
endif ()

include_directories("${GO_OUTPUT_BASE}/${CMAKE_ANDROID_ARCH_ABI}")
include_directories("${GO_SOURCE}")

link_directories("${GO_OUTPUT_BASE}/${CMAKE_ANDROID_ARCH_ABI}")

add_library(bridge SHARED main.c jni_helper.c bridge_helper.c)
target_link_libraries(bridge log clash)
