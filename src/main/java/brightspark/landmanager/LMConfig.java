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
    @Config.Comment("Whether non-op players can claim chunks using '/lm claim'")
    public static boolean disableClaiming = false;

    @Config.Comment("Whether non-op players in creative can break/place blocks in any area")
    public static boolean creativeIgnoresProtection = true;

    @Config.Comment("Max number of logs stored")
    public static int logStorageSize = 50;

    @Config.Comment("Client side configurations")
    public static final Client client = new Client();

    @Config.Comment("Non-OP player permissions for what they can change in their areas")
    public static final Permissions permissions = new Permissions();

    public static class Client
    {
        @Config.Comment("The radius within which nearby areas will show when /lmShow is showing all nearby areas")
        public int showAllRadius = 16;

        @Config.Comment("The scale of the area label that's rendered")
        public float areaNameScale = 1f;

        @Config.Comment("Whether OPs will see area changes in their chat")
        public boolean showChatLogs = true;
    }

    public static class Permissions
    {
        @Config.Comment({"If true then the 'claim' command will create a request rather than take instant effect",
                "An OP will then need to use the 'approve' command to accept the request"})
        public boolean claimRequest = false;

        @Config.Comment("If area owners can toggle whether passive entities can spawn in the area")
        @Config.RequiresMcRestart
        public boolean passiveSpawning = false;

        @Config.Comment("If area owners can toggle whether hostile entities can spawn in the area")
        @Config.RequiresMcRestart
        public boolean hostileSpawning = false;

        @Config.Comment("If area owners can toggle whether explosions can destroy blocks in the area")
        @Config.RequiresMcRestart
        public boolean explosions = false;

        @Config.Comment("If area owners can toggle whether other players can interact (right click) with blocks in the area")
        @Config.RequiresMcRestart
        public boolean interactions = false;

        @Config.Comment("If non-op players can use '/lm tool' to get the admin tool for creating areas")
        @Config.RequiresMcRestart
        public boolean tool = false;

        @Config.Comment("If area owners can set a cost for their area when selling it (if currency mod integration available)")
        @Config.RequiresMcRestart
        public boolean setCost = false;

        @Config.Comment("If area owners can rename their areas")
        @Config.RequiresMcRestart
        public boolean rename = false;
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
