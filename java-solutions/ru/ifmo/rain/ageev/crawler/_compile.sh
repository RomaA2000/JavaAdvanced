#!/usr/bin/env bash

cd ../../../../../../..

mod_name="ru.ifmo.rain.ageev.crawler"
mod_path="ru/ifmo/rain/ageev/crawler"
jar_name="_crawler"

root="$PWD"
out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/crawler/_build
req=${root}/java-advanced-2020/lib:${root}/java-advanced-2020/artifacts
src=${root}/java-advanced-2020-solutions/java-solutions
jar_out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/crawler
javac --module-path ${req} ${src}/module-info.java  ${src}/${mod_path}/WebCrawler.java -d ${out}
