#!/usr/bin/env bash

cd ../../../../../../..

project="$PWD"
lib="$project/java-advanced-2020/lib"
artifacts="$project/java-advanced-2020/artifacts"
modules="$project/java-advanced-2020-solutions/java-solutions"
my="$modules/ru/ifmo/rain/ageev/implementor"
kgeorgiy="$project/java-advanced-2020/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"
javadoc -private -link https://docs.oracle.com/en/java/javase/11/docs/api/ -d $project/java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/ageev/implementor/_javadoc --module-path \
  "$lib/quickcheck-0.6.jar:$lib/jsoup-1.8.1.jar:$lib/junit-4.11.jar:$lib/hamcrest-core-1.3.jar:$artifacts/info.kgeorgiy.java.advanced.implementor.jar:$artifacts/info.kgeorgiy.java.advanced.base.jar" \
  "$my/JarImplementor.java" \
  "$my/Implementor.java" \
  "$my/ImplementorDirectoryManager.java" \
  "$my/DirectoryCleaner.java" \
  "$my/MethodHasher.java" \
  "$my/ArgumentNumberMaker.java" \
  "$kgeorgiy/Impler.java" \
  "$kgeorgiy/JarImpler.java" \
  "$kgeorgiy/ImplerException.java"