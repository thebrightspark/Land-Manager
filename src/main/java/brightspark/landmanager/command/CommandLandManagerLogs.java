package brightspark.landmanager.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

/**
 * Created by bright_spark on 16/09/2018.
 */
public class CommandLandManagerLogs extends LMCommand
{
    @Override
    public String getName()
    {
        return "lmlog";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "\nlmlog [page] <areaName>";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        String area, page;
    }
}
