package dev.phonis.cannondebugextra.networking;

import dev.phonis.cannondebugextra.CannonDebugExtra;
import dev.phonis.cannondebugextra.excel.ExcelManager;
import net.minecraft.network.INetHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

public class CDChannel extends PluginChannel {

    public static CDChannel instance;

    private HistoryBuilder historyBuilder;

    public CDChannel(String name) {
        super(name);
    }

    public static void initialize() {
        CDChannel.instance = new CDChannel(CannonDebugExtra.channelName);
    }

    @Override
    public void onMessage(byte[] in, INetHandler netHandler) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(in));

        try {
            byte packetId = dis.readByte();

            switch (packetId) {
                case Packets.Out.startHistoryID:
                    this.handlePacket(CDStartHistory.fromBytes(dis));

                    break;
                case Packets.Out.historySegmentID:
                    this.handlePacket(CDHistorySegment.fromBytes(dis));

                    break;
                default:
                    System.out.println("Unrecognised packet.");

                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePacket(CDPacket packet) throws IOException {
        if (packet instanceof CDStartHistory) {
            CDStartHistory startHistory = (CDStartHistory) packet;
            this.historyBuilder = new HistoryBuilder(startHistory);
        } else if (packet instanceof CDHistorySegment) {
            CDHistorySegment historySegment = (CDHistorySegment) packet;

            if (this.historyBuilder == null) return;

            this.historyBuilder.addData(historySegment.payload);

            if (this.historyBuilder.isReady()) {
                CDHistory history = this.historyBuilder.getHistory();
//                FileOutputStream fos = new FileOutputStream("testcase.cdhistory");
//                DataOutputStream dos = new DataOutputStream(fos);
//
//                history.toBytes(dos);
//                dos.close();

                try {
                    ExcelManager.historyQueue.put(history);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void send(CDPacket packet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            dos.writeByte(packet.packetID());
            packet.toBytes(dos);
            this.sendToServer(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
