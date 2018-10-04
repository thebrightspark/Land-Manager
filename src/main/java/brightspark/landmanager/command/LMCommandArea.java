package brightspark.landmanager.command;

import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

//Used for commands which only have a single argument for an area
public abstract class LMCommandArea extends LMCommand
{
    private boolean canHaveNoArg = false;

    protected void setCanHaveNoArg()
    {
        canHaveNoArg = true;
    }

    @Override
    public final void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length == 0)
        {
            if(canHaveNoArg)
                execute(server, sender, null, null);
            else
                throwWrongUsage(sender);
            return;
        }

        String areaName = argsToString(args);
        if(areaName.isEmpty())
            throwWrongUsage(sender);

        CapabilityAreas cap = getWorldCapWithArea(server, areaName);
        Area area = cap.getArea(areaName);

        execute(server, sender, area, cap);
    }

    public abstract void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException;

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
    }

    protected void checkCanEditArea(MinecraftServer server, ICommandSender sender, Area area) throws CommandException
    {
        if(!isOP(server, sender) && !area.getAllocatedPlayer().equals(((EntityPlayer) sender).getUniqueID()))
            throw new CommandException("lm.command.noPerm", area.getName());
    }
}
