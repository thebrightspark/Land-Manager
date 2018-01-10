package brightspark.landmanager;

import brightspark.landmanager.gui.GuiHandler;
import brightspark.landmanager.item.LMItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = LandManager.MOD_ID, name = LandManager.MOD_NAME, version = LandManager.VERSION)
public class LandManager
{
    public static final String MOD_ID = "landmanager";
    public static final String MOD_NAME = "Land Manager";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static LandManager INSTANCE;
    public static Logger LOGGER;

    public static final CreativeTabs LM_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(LMItems.adminItem);
        }
    };

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        //TODO: Commands
    }
}
