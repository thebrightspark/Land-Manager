package brightspark.landmanager.command.op;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm op delete <areaName>
public class CommandDelete extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "delete";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.delete.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap)
    {
        if(cap.removeArea(area.getName()))
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.delete.deleted", area.getName()));
            LandManager.areaLog(AreaLogType.DELETE, area.getName(), sender);
        }
        else
            sender.sendMessage(new TextComponentTranslation("lm.command.delete.failed", area.getName()));
    }
}
