package dev.phonis.cannondebugextra.networking;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HistoryBuilder {

    private final int length;
    private final byte[] historyData;
    private int current = 0;

    public HistoryBuilder(int length) {
        this.length = length;
        this.historyData = new byte[this.length];
    }

    public HistoryBuilder(CDStartHistory startHistory) {
        this(startHistory.length);
    }

    public void addData(byte[] data) {
        System.arraycopy(data, 0, this.historyData, this.current, data.length);

        this.current += data.length;
    }

    public boolean isReady() {
        return this.current == this.length;
    }

    public CDHistory getHistory() throws IOException {
        return CDHistory.fromBytes(new DataInputStream(new ByteArrayInputStream(this.historyData)));
    }

}
