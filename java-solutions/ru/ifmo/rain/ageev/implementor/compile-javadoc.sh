#!/bin/bash
OUT_PROD="_javadoc"
MAIN_DIR="../../../../../../"
K_DIR="$MAIN_DIR/../java-advanced-2020"

LIB="$K_DIR/lib"
ARTS="$K_DIR/artifacts"
MY_PATH="ru.ifmo.rain.ageev.implementor"
IMPLEMENTOR="$K_DIR/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"

javadoc -d "$OUT_PROD" -private -link https://docs.oracle.com/en/java/javase/11/docs/api/ --module-path \
  "$LIB/quickcheck-0.6.jar:$LIB/jsoup-1.8.1.jar:$LIB/junit-4.11.jar:$LIB/hamcrest-core-1.3.jar:$ARTS/info.kgeorgiy.java.advanced.implementor.jar:$ARTS/info.kgeorgiy.java.advanced.base.jar" \
  "$MY_PATH/JarImplementor.java" "$MY_PATH/ArgumentNumberMaker.java" "$MY_PATH/DirectoryCleaner.java" "$MY_PATH/ImplementorDirectoryManager.java" "$MY_PATH/MethodHasher.java" \
  "$IMPLEMENTOR/Impler.java" \
  "$IMPLEMENTOR/JarImpler.java" \
  "$IMPLEMENTOR/ImplerException.java"
