package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CDStartHistory implements CDPacket {

    public final int length;

    public CDStartHistory(int length) {
        this.length = length;
    }

    @Override
    public byte packetID() {
        return Packets.Out.startHistoryID;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(this.length);
    }

    public static CDStartHistory fromBytes(DataInputStream dis) throws IOException {
        return new CDStartHistory(dis.readInt());
    }

}
