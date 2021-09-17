package dev.phonis.cannondebugextra.event;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatManager {

    public static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        String message;

        while ((message = ChatManager.messageQueue.poll()) != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

}
