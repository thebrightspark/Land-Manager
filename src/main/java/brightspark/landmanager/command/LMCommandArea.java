package brightspark.landmanager.command;

import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

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

        Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, areaName);

        execute(server, sender, pair.getRight(), pair.getLeft());
    }

    public abstract void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException;

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
    }
}
