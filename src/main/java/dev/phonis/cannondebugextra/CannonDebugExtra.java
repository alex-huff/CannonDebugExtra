package dev.phonis.cannondebugextra;

import dev.phonis.cannondebugextra.event.ChatManager;
import dev.phonis.cannondebugextra.networking.CDChannel;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = CannonDebugExtra.MODID, version = CannonDebugExtra.VERSION)
public class CannonDebugExtra {

    public static final String MODID = "cannondebugextra";
    public static final String VERSION = "1.0";
    public static final String channelName = "cannondebug:main";
    public static final String prefix =
        EnumChatFormatting.GRAY + "[" +
        EnumChatFormatting.AQUA + "CannonDebugExtra" +
        EnumChatFormatting.GRAY + "] " +
        EnumChatFormatting.RESET;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CDChannel.initialize();
        MinecraftForge.EVENT_BUS.register(new ChatManager());
    }

}
