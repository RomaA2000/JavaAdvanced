#!/bin/bash

cd ../../../../../../
ROOT=$PWD
SOLUTION_PATH=${ROOT}
PACKAGE_NAME=ru.ifmo.rain.ageev.bank
OUT_PATH=${SOLUTION_PATH}/java-solutions/ru/ifmo/rain/ageev/bank/_build
export CLASSPATH=${OUT_PATH}

java ${PACKAGE_NAME}.Server $@