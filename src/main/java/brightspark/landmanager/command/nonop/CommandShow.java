package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.message.MessageShowArea;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm show [areaName]
public class CommandShow extends LMCommandArea
{
    public CommandShow()
    {
        setCanHaveNoArg();
    }

    @Override
    public String getName()
    {
        return "show";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.show.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException
    {
        validateSenderIsPlayer(sender);

        if(area == null)
        {
            //Toggle showing all nearby areas
            LandManager.NETWORK.sendTo(new MessageShowArea(null), (EntityPlayerMP) sender);
        }
        else
        {
            //Show specific area
            LandManager.NETWORK.sendTo(new MessageShowArea(area.getName()), (EntityPlayerMP) sender);
            sender.sendMessage(new TextComponentTranslation("lm.command.show.showing", area.getName()));
        }
    }
}
