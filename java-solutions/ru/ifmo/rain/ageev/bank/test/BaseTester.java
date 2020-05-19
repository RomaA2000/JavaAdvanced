package ru.ifmo.rain.ageev.bank.test;


import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import java.util.ArrayList;
import java.util.List;

public class BaseTester {
    private final long start = System.currentTimeMillis();
    private final List<Class<?>> tests = new ArrayList<>();

    public void run() {
        final var result = new JUnitCore().run(tests.toArray(new Class<?>[0]));
        if (!result.wasSuccessful()) {
            for (final Failure failure : result.getFailures()) {
                System.err.println("Test " + failure.getDescription().getMethodName() + " failed: " + failure.getMessage());
                if (failure.getException() != null) {
                    failure.getException().printStackTrace();
                }
            }
            System.exit(1);
        } else {
            System.out.println("============================");
            final long time = System.currentTimeMillis() - start;
            System.out.println("All tests passed in " + time);
            System.exit(0);
        }
    }

    public BaseTester add(final Class<?> testClass) {
        tests.add(testClass);
        return this;
    }
}
