#!/bin/bash

PROJ_DIR="../../../../../../"
LIB_DIR="$PROJ_DIR/../java-advanced-2020"
LIB="$LIB_DIR/lib"
ARTS="$LIB_DIR/artifacts"

BUILD_OUT="_build"

javac -d "$BUILD_OUT" -p "$ARTS":"$LIB" --module-source-path . --module ru.ifmo.rain.ageev.implementor || exit 1

cd "$BUILD_OUT" || exit 1

jar -c --file=../_implementor.jar .
