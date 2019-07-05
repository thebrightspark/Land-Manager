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

    @Config.Comment({
	    "The max number of areas a player can own",
	    "Use -1 for no limit"
    })
    @Config.RangeInt(min = -1)
    public static int maxAreasCanOwn = -1;

    @Config.Comment("Max number of logs stored")
    public static int logStorageSize = 50;

    @Config.Comment("Client side configurations")
    public static final Client client = new Client();

    @Config.Comment("Non-OP player permissions for what they can change in their areas")
    public static final Permissions permissions = new Permissions();

	@Config.Comment("Global settings affect all blocks outside of any areas")
    public static final GlobalSettings globalSettings = new GlobalSettings();

    public static class Client
    {
        @Config.Comment("The radius within which nearby areas will show when /lmShow is showing all nearby areas")
        @Config.RangeInt(min = 0)
        public int showAllRadius = 16;

        @Config.Comment("The scale of the area label that's rendered")
        public float areaNameScale = 1f;

        @Config.Comment("Whether OPs will see area changes in their chat")
        public boolean showChatLogs = true;

        @Config.Comment("The alpha for the sides of area boxes rendered in the world")
        @Config.RangeDouble(min = 0D, max = 1D)
        public float areaBoxAlpha = 0.2F;

        @Config.Comment("The thickness of area box edges rendered in the world")
        @Config.RangeDouble(min = 0D)
        public double areaBoxEdgeThickness = 0.025D;
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

        @Config.Comment("If area owners can rename their areas")
        @Config.RequiresMcRestart
        public boolean rename = false;
    }

    public static class GlobalSettings
    {
    	@Config.Comment("Can passive entities spawn in global spaces")
	    public boolean canPassiveSpawn = true;

	    @Config.Comment("Can hostile entities spawn in global spaces")
	    public boolean canHostileSpawn = true;

	    @Config.Comment("Can explosions destroy global blocks")
	    public boolean canExplosionsDestroyBlocks = true;

	    @Config.Comment("Can players interact with global blocks")
	    public boolean canPlayersInteract = true;

	    @Config.Comment("Can players break global blocks")
	    public boolean canPlayersBreakBlocks = true;

	    @Config.Comment("Can players place global blocks")
	    public boolean canPlayersPlaceBlocks = true;
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
