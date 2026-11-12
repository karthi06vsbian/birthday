cmake_minimum_required(VERSION 3.22.1)

project("ffgamingcompanion")

add_library(
    native-engine
    SHARED
    native-engine.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    native-engine
    ${log-lib}
)
