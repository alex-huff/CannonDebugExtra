package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CDHistorySegment implements CDPacket {

    public static final short maxLength = 30000;

    public final int length;
    public final byte[] payload;

    public CDHistorySegment(byte[] payload) {
        this.length = payload.length;
        this.payload = payload;
    }

    @Override
    public byte packetID() {
        return Packets.Out.historySegmentID;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(this.length);
        dos.write(this.payload);
    }

    public static CDHistorySegment fromBytes(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        byte[] data = new byte[size];

        dis.readFully(data);

        return new CDHistorySegment(data);
    }

}