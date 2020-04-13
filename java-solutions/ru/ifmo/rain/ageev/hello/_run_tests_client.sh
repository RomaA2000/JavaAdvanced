#!/usr/bin/env bash
./_compile.sh
root="../../../../../../../.."
out="_build"
kgeorgiy_source="java-advanced-2020"

req=$root/$kgeorgiy_source/artifacts:$root/$kgeorgiy_source/lib

cd $out || exit

java -cp . -p . --module-path=$req \
    --add-modules info.kgeorgiy.java.advanced.hello \
    -m info.kgeorgiy.java.advanced.hello client-i18n ru.ifmo.rain.ageev.hello.HelloUDPClient $1