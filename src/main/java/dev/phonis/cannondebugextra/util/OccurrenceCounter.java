package dev.phonis.cannondebugextra.util;

public class OccurrenceCounter {

    private int numOccurrences;

    public OccurrenceCounter(int initial) {
        this.numOccurrences = initial;
    }

    public int getNumOccurrences() {
        return this.numOccurrences;
    }

    public void incrementOccurrences() {
        this.numOccurrences += 1;
    }

}