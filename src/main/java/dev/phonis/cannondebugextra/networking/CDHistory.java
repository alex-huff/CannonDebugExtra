package dev.phonis.cannondebugextra.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CDHistory implements CDSerializable {

    public final List<CDBlockSelection> selections;

    public CDHistory(List<CDBlockSelection> selections) {
        this.selections = selections;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(this.selections.size());

        for (CDBlockSelection selection : this.selections) {
            selection.toBytes(dos);
        }
    }

    public static CDHistory fromBytes(DataInputStream dis) throws IOException {
        int numBlockSelections = dis.readInt();
        List<CDBlockSelection> selections = new ArrayList<>();

        for (int i = 0; i < numBlockSelections; i++) {
            selections.add(CDBlockSelection.fromBytes(dis));
        }

        return new CDHistory(selections);
    }

}