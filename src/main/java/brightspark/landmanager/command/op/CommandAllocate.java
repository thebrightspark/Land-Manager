package brightspark.landmanager.command.op;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

//lm op allocate <playerName> <areaName>
public class CommandAllocate extends LMCommand
{
    @Override
    public String getName()
    {
        return "allocate";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.allocate.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length < 2)
            throwWrongUsage(sender);

        String areaName = argsToString(args, 1);
        if(areaName.isEmpty())
            throwWrongUsage(sender);

        CapabilityAreas cap = getWorldCapWithArea(server, areaName);
        String playerName = args[0];
        UUID uuid = getUuidFromPlayerName(server, playerName);
        if(cap.setAllocation(areaName, uuid))
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.allocate.success", areaName, playerName));
            LandManager.areaLog(AreaLogType.ALLOCATE, areaName, sender);
        }
        else
            sender.sendMessage(new TextComponentTranslation("lm.command.allocate.failed", areaName));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        switch(args.length)
        {
            case 0:
                return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
            case 1:
                return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
            default:
                return Collections.emptyList();
        }
    }
}
