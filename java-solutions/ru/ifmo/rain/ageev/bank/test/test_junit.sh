#!/bin/bash

cd ../../../../../../..

root=$PWD

my_name=ru.ifmo.rain.ageev.bank
my_path=ru/ifmo/rain/ageev/bank

out_my_path=${root}/java-solutions/ru/ifmo/rain/ageev/bank/_build/
src=${root}/java-solutions/${my_path}
lib=${root}/../java-advanced-2020/lib
junit=${lib}/junit-4.11.jar:${lib}/hamcrest-core-1.3.jar

rm -rf ${out_my_path}

echo ${out_my_path}

javac -cp .:${junit} \
      ${src}/*.java ${src}/*.java ${src}/interfaces/*.java ${src}/classes/*.java ${src}/test/*.java -d ${out_my_path}

cd ${out_my_path} || exit 1

java -cp .:${junit} org.junit.runner.JUnitCore ${my_name}.test.Tests

exit ${?}