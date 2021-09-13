package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CDEntityTracker implements CDSerializable {

    public final CDEntityType entityType;
    public final long spawnTick;
    public final List<CDLocation> locationHistory;
    public final List<CDVec3D> velocityHistory;
    public long deathTick;

    public CDEntityTracker(CDEntityType entityType, long spawnTick, List<CDLocation> locationHistory, List<CDVec3D> velocityHistory, long deathTick) {
        this.entityType = entityType;
        this.spawnTick = spawnTick;
        this.locationHistory = locationHistory;
        this.velocityHistory = velocityHistory;
        this.deathTick = deathTick;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        this.entityType.toBytes(dos);
        dos.writeLong(this.spawnTick);
        dos.writeInt(this.locationHistory.size());

        for (CDLocation location : this.locationHistory) {
            location.toBytes(dos);
        }

        dos.writeInt(this.velocityHistory.size());

        for (CDVec3D velocity : this.velocityHistory) {
            velocity.toBytes(dos);
        }

        dos.writeLong(this.deathTick);
    }

    public static CDEntityTracker fromBytes(DataInputStream dis) throws IOException {
        return new CDEntityTracker(
            CDEntityType.fromBytes(dis),
            dis.readLong(),
            CDEntityTracker.locationHistoryFromBytes(dis),
            CDEntityTracker.velocityHistoryFromBytes(dis),
            dis.readLong()
        );
    }

    private static List<CDLocation> locationHistoryFromBytes(DataInputStream dis) throws IOException {
        int numLocation = dis.readInt();
        List<CDLocation> locations = new ArrayList<>(numLocation);

        for (int i = 0; i < numLocation; i++) {
            locations.add(CDLocation.fromBytes(dis));
        }

        return locations;
    }

    private static List<CDVec3D> velocityHistoryFromBytes(DataInputStream dis) throws IOException {
        int numLocation = dis.readInt();
        List<CDVec3D> velocities = new ArrayList<>(numLocation);

        for (int i = 0; i < numLocation; i++) {
            velocities.add(CDVec3D.fromBytes(dis));
        }

        return velocities;
    }

}
