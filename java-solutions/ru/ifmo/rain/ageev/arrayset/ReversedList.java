package ru.ifmo.rain.ageev.arrayset;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class ReversedList<E> extends AbstractList<E> implements RandomAccess {
    private boolean reversed;
    private final List<E> elements;

    public ReversedList(List<E> other, boolean needToReverse) {
        elements = Collections.unmodifiableList(other);
        reversed = needToReverse;
    }

    public ReversedList(List<E> other) {
        this(other, false);
    }

    public ReversedList(ReversedList<E> other) {
        this(other.elements, !other.reversed);
    }

    @Override
    public E get(int index) {
        return reversed ? elementGetter(size() - 1 - index) : elementGetter(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    private E elementGetter(int index) {
        return elements.get(index);
    }
}
