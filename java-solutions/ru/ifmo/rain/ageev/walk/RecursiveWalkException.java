package ru.ifmo.ageev.walk;

public class RecursiveWalkException extends Exception {
    RecursiveWalkException(final String information, final Throwable cause) {
        super(information, cause);
    }

    RecursiveWalkException(final String information) {
        super(information);
    }
}
