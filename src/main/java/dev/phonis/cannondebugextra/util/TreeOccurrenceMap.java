package dev.phonis.cannondebugextra.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class TreeOccurrenceMap<T extends Comparable<T>> implements OccurrenceMap<T> {

    private final Map<T, OccurrenceCounter> perTickOccurrences = new TreeMap<>();

    private OccurrenceCounter getOrCreate(T key) {
        OccurrenceCounter counter;

        if (this.perTickOccurrences.containsKey(key)) {
            counter = this.perTickOccurrences.get(key);
        } else {
            counter = new OccurrenceCounter(0);

            this.perTickOccurrences.put(key, counter);
        }

        return counter;
    }

    @Override
    public void increment(T key) {
        this.getOrCreate(key).incrementOccurrences();
    }

    @Override
    public Integer getOccurrences(T key) {
        if (this.perTickOccurrences.containsKey(key))
            return this.perTickOccurrences.get(key).getNumOccurrences();

        return 0;
    }

    @Override
    public Pair<T, Integer> getMin() {
        Map.Entry<T, OccurrenceCounter> minEntry = this.perTickOccurrences.entrySet().parallelStream().min(
            Comparator.comparingInt(entry -> entry.getValue().getNumOccurrences())
        ).orElseThrow(() -> new IllegalStateException("Empty OccurrenceMap"));

        return new ImmutablePair<>(minEntry.getKey(), minEntry.getValue().getNumOccurrences());
    }

    @Override
    public Pair<T, Integer> getMax() {
        Map.Entry<T, OccurrenceCounter> maxEntry = this.perTickOccurrences.entrySet().parallelStream().max(
            Comparator.comparingInt(entry -> entry.getValue().getNumOccurrences())
        ).orElseThrow(() -> new IllegalStateException("Empty OccurrenceMap"));

        return new ImmutablePair<>(maxEntry.getKey(), maxEntry.getValue().getNumOccurrences());
    }

}
