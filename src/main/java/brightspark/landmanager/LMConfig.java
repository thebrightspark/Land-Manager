package brightspark.landmanager;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = LandManager.MOD_ID)
@Config.LangKey(LandManager.MOD_ID + ".config.title")
public class LMConfig
{
    @Config.Comment("Whether non-op players can claim chunks using /lmClaim")
    public static boolean disableClaiming = false;

    @Config.Comment("Whether non-op players in creative can break/place blocks in any area")
    public static boolean creativeIgnoresProtection = true;

    @Config.Comment("Max number of logs stored")
    public static int logStorageSize = 50;

    public static final Client client = new Client();

    public static class Client
    {
        @Config.Comment("The radius within which nearby areas will show when /lmShow is showing all nearby areas")
        public int showAllRadius = 16;

        @Config.Comment("The scale of the area label that's rendered")
        public float areaNameScale = 1f;

        @Config.Comment("Whether OPs will see area changes in their chat")
        public boolean showChatLogs = true;
    }

    @Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
    private static class Handler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if(event.getModID().equals(LandManager.MOD_ID))
                ConfigManager.sync(LandManager.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
