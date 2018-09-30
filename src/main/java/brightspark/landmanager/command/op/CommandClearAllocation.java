package brightspark.landmanager.command.op;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm op clearAllocation <areaName>
public class CommandClearAllocation extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "clearallocation";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.clear.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap)
    {
        area.setAllocatedPlayer(null);
        cap.dataChanged();
        sender.sendMessage(new TextComponentTranslation("lm.command.clear.cleared", area.getName()));
        LandManager.areaLog(AreaLogType.CLEAR_ALLOCATION, area.getName(), sender);
    }
}
