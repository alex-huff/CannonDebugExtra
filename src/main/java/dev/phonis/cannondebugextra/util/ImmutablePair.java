package dev.phonis.cannondebugextra.util;

public class ImmutablePair<L, R> implements Pair<L, R> {

    private final L left;
    private final R right;

    public ImmutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    public R getRight() {
        return this.right;
    }

    @Override
    public void setLeft(L left) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRight(R right) {
        throw new UnsupportedOperationException();
    }

}
