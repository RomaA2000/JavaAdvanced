#!/usr/bin/env bash

cd ../../../../../../..

mod_name="ru.ifmo.rain.ageev.concurrent"
mod_path="ru/ifmo/rain/ageev/concurrent"
jar_name="_concurrent"

root="$PWD"
out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/concurrent/_build
req=${root}/java-advanced-2020/lib:${root}/java-advanced-2020/artifacts
src=${root}/java-advanced-2020-solutions/java-solutions
jar_out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/concurrent
javac --module-path ${req} ${src}/module-info.java  ${src}/${mod_path}/IterativeParallelism.java  -d ${out}
cd ${out}
jar -c --file=${jar_out}/${jar_name}.jar --main-class=${mod_name}.IterativeParallelism --module-path=${req} module-info.class ${mod_path}/IterativeParallelism.class
cd ${jar_out}