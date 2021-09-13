package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CDBlockSelection implements CDSerializable {

    public final int id;
    public final CDLocation location;
    public final int order;
    public final CDEntityTracker tracker;

    public CDBlockSelection(int id, CDLocation location, int order, CDEntityTracker tracker) {
        this.id = id;
        this.location = location;
        this.order = order;
        this.tracker = tracker;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(this.id);
        this.location.toBytes(dos);
        dos.writeInt(this.order);
        this.tracker.toBytes(dos);
    }

    public static CDBlockSelection fromBytes(DataInputStream dis) throws IOException {
        return new CDBlockSelection(
            dis.readInt(),
            CDLocation.fromBytes(dis),
            dis.readInt(),
            CDEntityTracker.fromBytes(dis)
        );
    }

}