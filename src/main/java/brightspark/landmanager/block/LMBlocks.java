package brightspark.landmanager.block;

import brightspark.landmanager.LandManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class LMBlocks
{
	public static final Block home = new BlockHome();

	@SubscribeEvent
	public static void regBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(home);
	}

	@SubscribeEvent
	public static void regItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().register(new ItemBlock(home).setRegistryName(home.getRegistryName()));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void regModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(home), 0, new ModelResourceLocation(home.getRegistryName(), "inventory"));
	}
}
