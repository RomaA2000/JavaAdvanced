package ru.ifmo.rain.ageev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Hash providing class method wrapper. Custom equality check for {@link Method}.
 *
 * @see Method#hashCode()
 */
public class MethodHasher {
    /**
     * Prime multiplier used in hashing.
     */
    private static final int HASH_PRIME_NUMBER = 239;
    /**
     * Inner wrapped {@link Method} instance.
     */
    private final Method method;

    /**
     * Wrapping constructor. Creates new instance of {@link MethodHasher} with wrapped {@link Method} inside.
     *
     * @param method instance of {@link Method} class
     */
    MethodHasher(Method method) {
        this.method = method;
    }

    /**
     * Getter for wrapped instance of {@link Method} class.
     *
     * @return wrapped {@link #method}
     */
    public Method get() {
        return method;
    }

    /**
     * Provides hash code calculator. Calculates hash code for wrapped {@link #method}
     * using it's name, parameter types and return type.
     *
     * @return integer value
     */
    public int hashCode() {
        return method.getName().hashCode() +
                HASH_PRIME_NUMBER * method.getReturnType().hashCode() +
                HASH_PRIME_NUMBER * HASH_PRIME_NUMBER * Arrays.hashCode(method.getParameterTypes());
    }

    /**
     * Checks if {@link #method} is equal to another object.
     * Object is considered equal if it is an instance of {@link MethodHasher}
     * and has a wrapped {@link #method} inside with same name, parameter types and return type.
     *
     * @param o object to compare with
     * @return <code>true</code> if objects ans method are equal, <code>false</code> otherwise
     */
    public boolean equals(Object o) {
        try {
            MethodHasher m = (MethodHasher) o;
            return method.getName().equals(m.get().getName()) &&
                    method.getReturnType().equals(m.get().getReturnType()) &&
                    Arrays.equals(method.getParameterTypes(), m.get().getParameterTypes());
        } catch (ClassCastException e) {
            return false;
        }
    }
}