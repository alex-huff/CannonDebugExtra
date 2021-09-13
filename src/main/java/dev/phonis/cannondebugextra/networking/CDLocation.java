package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.IOException;

public class CDLocation extends CDVec3D {

    public CDLocation(double x, double y, double z) {
        super(x, y, z);
    }

    public static CDLocation fromBytes(DataInputStream dis) throws IOException {
        return new CDLocation(dis.readDouble(), dis.readDouble(), dis.readDouble());
    }

}
