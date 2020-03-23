#!/usr/bin/env bash

cd ../../../../../../..

mod_name="ru.ifmo.rain.ageev.implementor"
mod_path="ru/ifmo/rain/ageev/implementor"
jar_name="_implementor"

root="$PWD"
out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/implementor/_build
req=${root}/java-advanced-2020/lib:${root}/java-advanced-2020/artifacts
src=${root}/java-advanced-2020-solutions/java-solutions
jar_out=${root}/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/implementor
javac --module-path ${req} ${src}/module-info.java  ${src}/${mod_path}/JarImplementor.java ${src}/${mod_path}/Implementor.java ${src}/${mod_path}/ImplementorDirectoryManager.java  ${src}/${mod_path}/DirectoryCleaner.java  ${src}/${mod_path}/MethodHasher.java -d ${out}
cd ${out}
jar -c --file=${jar_out}/${jar_name}.jar --main-class=${mod_name}.JarImplementor --module-path=${req} module-info.class ${mod_path}/JarImplementor.class
cd ${jar_out}