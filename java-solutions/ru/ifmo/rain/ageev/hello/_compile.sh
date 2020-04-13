#!/usr/bin/env bash

cd ../../../../../../..

mod_name="ru.ifmo.rain.ageev.hello"
mod_path="ru/ifmo/rain/ageev/hello"
jar_name="_hello"

root="$PWD"
out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/hello/_build
req=${root}/java-advanced-2020/lib:${root}/java-advanced-2020/artifacts
src=${root}/java-advanced-2020-solutions/java-solutions
jar_out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/hello
javac --module-path ${req} ${src}/module-info.java  ${src}/${mod_path}/HelloUDPClient.java ${src}/${mod_path}/HelloUDPServer.java  ${src}/${mod_path}/NetUtils.java ${src}/${mod_path}/ClientWorker.java -d ${out}
