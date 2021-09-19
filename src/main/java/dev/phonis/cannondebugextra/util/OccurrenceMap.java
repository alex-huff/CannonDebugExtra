package dev.phonis.cannondebugextra.util;

public interface OccurrenceMap<T> {

    void increment(T key);

    Integer getOccurrences(T key);

    Pair<T, Integer> getMin() throws IllegalStateException;

    Pair<T, Integer> getMax() throws IllegalStateException;

}
