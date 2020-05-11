#!/bin/bash

cd ../../../../../../..

mod_name="ru.ifmo.rain.ageev.bank"
mod_path="ru/ifmo/rain/ageev/bank"

root="$PWD"
out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/bank/_build
req=${root}/java-advanced-2020/lib:${root}/java-advanced-2020/artifacts
src=${root}/java-advanced-2020-solutions/java-solutions
jar_out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/bank
javac --module-path ${req} ${src}/module-info.java  ${src}/${mod_path}/*.java ${src}/${mod_path}/classes/*.java  ${src}/${mod_path}/interfaces/*.java -d ${out}
