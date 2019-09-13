package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm interactions <areaName>
//OR
//lm op interactions <areaName>
public class CommandInteractions extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "interactions";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return LMConfig.permissions.interactions ?  "lm.command.interactions.usage" : "lm.command.interactions.usage.op";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return getPermissionLevel(LMConfig.permissions.interactions);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException
    {
        checkCanEditArea(server, sender, area);

        area.toggleInteractions();
        cap.dataChanged();
        sender.sendMessage(new TextComponentTranslation("lm.command.interactions.success", area.canInteract(), area.getName()));
        LandManager.areaLog(AreaLogType.SET_EXPLOSIONS, area.getName(), sender);
    }
}
