package ru.ifmo.ageev.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final ReversedList<E> elements;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super E> otherComparator) {
        this(Collections.emptyList(), otherComparator);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> otherComparator) {
        comparator = getValidComparator(otherComparator);
        TreeSet<E> treeSet = new TreeSet<>(otherComparator);
        treeSet.addAll(collection);
        elements = new ReversedList<>(new ArrayList<>(treeSet));
    }

    private ArraySet(List<E> list, Comparator<? super E> otherComparator) {
        this(new ReversedList<>(list), otherComparator);
    }

    private ArraySet(ReversedList<E> list, Comparator<? super E> otherComparator) {
        elements = list;
        comparator = otherComparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return getIndex((E) o) >= 0;
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public E lower(E e) {
        return checkedGet(lowerIdx(e));
    }

    @Override
    public E floor(E e) {
        return checkedGet(floorIdx(e));
    }

    @Override
    public E ceiling(E e) {
        return checkedGet(ceilingIdx(e));
    }

    @Override
    public E higher(E e) {
        return checkedGet(higherIdx(e));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Poll First is not supported");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Poll Last is not supported");
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedList<>(elements), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Left element shouldn't be greater than right");
        }
        return implSubSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return implHeadTail(toElement, inclusive, true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return implHeadTail(fromElement, inclusive, false);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        return implFirstLast(0);
    }

    @Override
    public E last() {
        return implFirstLast(size() - 1);
    }

    @Override
    public int size() {
        return elements.size();
    }

    private E implFirstLast(int i) {
        checkEmpty();
        return elements.get(i);
    }

    private NavigableSet<E> implSubSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        checkForNull(fromElement, toElement);
        int left = fromInclusive ? ceilingIdx(fromElement) : higherIdx(fromElement);
        int right = toInclusive ? floorIdx(toElement) : lowerIdx(toElement);
        boolean flag = (right == -1 || left == -1 || left > right);
        return flag ? new ArraySet<>(comparator) : new ArraySet<>(elements.subList(left, right + 1), comparator);
    }

    private NavigableSet<E> implHeadTail(E e, boolean inclusive, boolean head) {
        if (isEmpty()) {
            return this;
        }
        return head ? implSubSet(first(), true, e, inclusive) : implSubSet(e, inclusive, last(), true);
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("Access to an element of an empty container");
        }
    }

    private int getIndex(E e) {
        checkForNull(e);
        return Collections.binarySearch(elements, e, comparator);
    }

    private boolean checkInd(int idx) {
        return 0 <= idx && idx < elements.size();
    }

    private E checkedGet(int idx) {
        return checkInd(idx) ? elements.get(idx) : null;
    }

    private int toValidIdx(int ans, int idx) {
        return checkInd(ans + idx) ? ans + idx : -1;
    }

    private int findIdx(E e, int notFound, int found) {
        int ans = getIndex(e);
        if (ans < 0) {
            ans = -ans - 1;
            return toValidIdx(ans, notFound);
        }
        return toValidIdx(ans, found);
    }

    private void checkForNull(E e) {
        Objects.requireNonNull(e, "Not null element expected");
    }

    private void checkForNull(E e1, E e2) {
        checkForNull(e1);
        checkForNull(e2);
    }

    private int lowerIdx(E e) {
        return findIdx(e, -1, -1);
    }

    private int floorIdx(E e) {
        return findIdx(e, -1, 0);
    }

    private int ceilingIdx(E e) {
        return findIdx(e, 0, 0);
    }

    private Comparator<? super E> getValidComparator(Comparator<? super E> cmp) {
        return Comparator.naturalOrder() == cmp ? null : cmp;
    }

    @SuppressWarnings("unchecked")
    private int compare(E e1, E e2) {
        return (comparator == null) ? ((Comparable<E>) e1).compareTo(e2) : comparator.compare(e1, e2);
    }

    private int higherIdx(E e) {
        return findIdx(e, 0, 1);
    }
}
