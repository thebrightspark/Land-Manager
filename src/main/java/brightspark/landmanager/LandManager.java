package brightspark.landmanager;

import brightspark.landmanager.command.CommandLM;
import brightspark.landmanager.data.areas.CapStorage;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.areas.CapabilityAreasImpl;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.data.logs.LogsWorldSavedData;
import brightspark.landmanager.gui.GuiHandler;
import brightspark.landmanager.item.LMItems;
import brightspark.landmanager.message.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(modid = LandManager.MOD_ID, name = LandManager.MOD_NAME, version = LandManager.VERSION)
public class LandManager
{
    public static final String MOD_ID = "landmanager";
    public static final String MOD_NAME = "Land Manager";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static LandManager INSTANCE;
    public static Logger LOGGER;
    public static SimpleNetworkWrapper NETWORK;
    private static int messageId = 0;

    @CapabilityInject(CapabilityAreas.class)
    public static Capability<CapabilityAreas> CAPABILITY_AREAS = null;

    public static final CreativeTabs LM_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(LMItems.adminItem);
        }
    };

    private static <REQ extends IMessage, REPLY extends IMessage> void regMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side receivingSide, Side thisSide)
    {
        IMessageHandler<? super REQ, ? extends REPLY> handler =
            thisSide == Side.CLIENT || (receivingSide == Side.SERVER && thisSide == Side.SERVER) ?
                instantiate(messageHandler) :
                new DummyHandler<>();
        NETWORK.registerMessage(handler, requestMessageType, messageId++, receivingSide);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler)
    {
        try
        {
            return handler.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        regMessage(MessageCreateArea.Handler.class, MessageCreateArea.class, Side.SERVER, side);
        regMessage(MessageCreateAreaReply.Handler.class, MessageCreateAreaReply.class, Side.CLIENT, side);
        regMessage(MessageUpdateCapability.Handler.class, MessageUpdateCapability.class, Side.CLIENT, side);
        regMessage(MessageShowArea.Handler.class, MessageShowArea.class, Side.CLIENT, side);
        regMessage(MessageChatLog.Handler.class, MessageChatLog.class, Side.CLIENT, side);
        regMessage(MessageOpenHomeGui.Handler.class, MessageOpenHomeGui.class, Side.CLIENT, side);
        regMessage(MessageHomeActionKickOrPass.Handler.class, MessageHomeActionKickOrPass.class, Side.SERVER, side);
        regMessage(MessageHomeActionAdd.Handler.class, MessageHomeActionAdd.class, Side.SERVER, side);
        regMessage(MessageHomeActionReply.Handler.class, MessageHomeActionReply.class, Side.CLIENT, side);
        regMessage(MessageHomeToggle.Handler.class, MessageHomeToggle.class, Side.SERVER, side);
        regMessage(MessageHomeToggleReply.Handler.class, MessageHomeToggleReply.class, Side.CLIENT, side);
        regMessage(MessageHomeActionReplyError.Handler.class, MessageHomeActionReplyError.class, Side.CLIENT, side);
        regMessage(MessageAreaAdd.Handler.class, MessageAreaAdd.class, Side.CLIENT, side);
        regMessage(MessageAreaChange.Handler.class, MessageAreaChange.class, Side.CLIENT, side);
        regMessage(MessageAreaDelete.Handler.class, MessageAreaDelete.class, Side.CLIENT, side);
        regMessage(MessageAreaRename.Handler.class, MessageAreaRename.class, Side.CLIENT, side);

        CapabilityManager.INSTANCE.register(CapabilityAreas.class, new CapStorage<>(), CapabilityAreasImpl::new);

        //Register ore dictionary entry for carpets for the Home block recipe
        OreDictionary.registerOre("carpet", new ItemStack(Blocks.CARPET, 1, OreDictionary.WILDCARD_VALUE));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLM());
        //event.registerServerCommand(new CommandLandManagerLogs());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        //Ensure that area names do not contain whitespaces
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null)
                continue;
            cap.getAllAreas().forEach(area ->
            {
                String name = area.getName();
                if(name.contains(" "))
                    area.setName(name.replaceAll(" ", "_"));
            });
        }
    }

    public static void areaLog(AreaLogType type, String areaName, ICommandSender sender)
    {
        LogsWorldSavedData logData = LogsWorldSavedData.get(sender.getEntityWorld());
        if(logData != null)
            logData.addLog(type, areaName, sender);
    }

    public static void sendToOPs(MinecraftServer server, Supplier<IMessage> message, @Nullable EntityPlayer sender)
    {
        doForEachOP(server, op -> LandManager.NETWORK.sendTo(message.get(), op), sender);
    }

    public static void sendChatMessageToOPs(MinecraftServer server, ITextComponent message, @Nullable EntityPlayer sender)
    {
        doForEachOP(server, op -> op.sendMessage(message), sender);
    }

    private static void doForEachOP(MinecraftServer server, Consumer<EntityPlayerMP> toDoForOP, @Nullable EntityPlayer sender)
    {
        String[] ops = server.getPlayerList().getOppedPlayers().getKeys();
        for(String op : ops)
        {
            EntityPlayerMP playerOp = server.getPlayerList().getPlayerByUsername(op);
            if(playerOp != null && !playerOp.equals(sender))
                toDoForOP.accept(playerOp);
        }
    }
}
