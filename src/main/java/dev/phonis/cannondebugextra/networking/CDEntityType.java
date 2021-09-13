package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum CDEntityType implements CDSerializable {

    TNT, FALLINGBLOCK, OTHER;

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeByte(this.ordinal());
    }

    public static CDEntityType fromBytes(DataInputStream dis) throws IOException {
        return CDEntityType.values()[dis.readByte()];
    }

}
