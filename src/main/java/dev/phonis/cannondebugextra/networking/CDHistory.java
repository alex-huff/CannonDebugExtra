package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CDHistory implements CDSerializable {

    public final List<CDBlockSelection> selections;
    public final boolean byOrder;

    public CDHistory(List<CDBlockSelection> selections, boolean byOrder) {
        this.selections = selections;
        this.byOrder = byOrder;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(this.selections.size());

        for (CDBlockSelection selection : this.selections) {
            selection.toBytes(dos);
        }

        dos.writeBoolean(this.byOrder);
    }

    public static CDHistory fromBytes(DataInputStream dis) throws IOException {
        int numBlockSelections = dis.readInt();
        List<CDBlockSelection> selections = new ArrayList<>();

        for (int i = 0; i < numBlockSelections; i++) {
            selections.add(CDBlockSelection.fromBytes(dis));
        }

        return new CDHistory(selections, dis.readBoolean());
    }

}