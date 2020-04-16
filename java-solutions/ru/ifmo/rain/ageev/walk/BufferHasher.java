package ru.ifmo.ageev.walk;

public class BufferHasher {
    private static final int PRIME_NUMBER_BIG = 0x01000193;
    private static final int PRIME_NUMBER_SMALL = 0xff;
    private static final int START_NUMBER = 0x811c9dc5;
    private int hash = START_NUMBER;

    public void hash(final byte[] buffer, int size) {
        for (int i = 0; i < size; ++i) {
            hash = (hash * PRIME_NUMBER_BIG) ^ (buffer[i] & PRIME_NUMBER_SMALL);
        }
    }

    public int getHash() {
        return hash;
    }
}
