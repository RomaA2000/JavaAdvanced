package ru.ifmo.rain.ageev.implementor;

/**
 * Integer values supplier class. Returns consecutive integer values.
 * Is used for argument names generation in {@link JarImplementor}.
 */

class ArgumentNumberMaker {
    /**
     * Integer value used for consecutive numbers generation.
     */
    private Integer idx;

    /**
     * Default constructor. Creates new instance of {@link ArgumentNumberMaker} with {@link #idx} set to zero.
     */
    ArgumentNumberMaker() {
        idx = 0;
    }

    /**
     * Index getter method. Returns next integer from {@link #idx}.
     *
     * @return casted to string next integer value
     */
    public String get() {
        return (idx++).toString();
    }
}
