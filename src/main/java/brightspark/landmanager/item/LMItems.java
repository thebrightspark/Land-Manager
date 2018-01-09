package brightspark.landmanager.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class LMItems
{
    public static final Item adminItem = new ItemAdmin();

    @SubscribeEvent
    public static void regItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(adminItem);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void regModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(adminItem, 0, new ModelResourceLocation(adminItem.getRegistryName(), "inventory"));
    }
}
