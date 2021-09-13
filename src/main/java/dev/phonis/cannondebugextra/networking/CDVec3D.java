package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CDVec3D implements CDSerializable {

    public final double x;
    public final double y;
    public final double z;

    public CDVec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeDouble(this.x);
        dos.writeDouble(this.y);
        dos.writeDouble(this.z);
    }

    public static CDVec3D fromBytes(DataInputStream dis) throws IOException {
        return new CDVec3D(dis.readDouble(), dis.readDouble(), dis.readDouble());
    }

}
