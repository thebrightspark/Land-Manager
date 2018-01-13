package brightspark.landmanager.command;

import brightspark.landmanager.handler.ClientEventHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandShow extends LMCommand
{
    @Override
    public String getName()
    {
        return "lmShow";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lmShow [areaName]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentString("You need to be a player to use this command"));
            return;
        }

        if(args.length == 0)
        {
            //Show all nearby areas
            if(ClientEventHandler.toggleRenderAll())
                sender.sendMessage(new TextComponentString("Now showing all nearby areas"));
            else
                sender.sendMessage(new TextComponentString("Turned off showing all nearby areas"));
        }
        else
        {
            //Show specific area
            String areaName = argsToString(args, 0);
            if(areaName.isEmpty())
                sender.sendMessage(new TextComponentString("Invalid area name provided"));
            else
            {
                //Make sure it's a valid area name
                List<String> areas = getAllAreaNames(server);
                if(areas.contains(areaName))
                {
                    ClientEventHandler.setRenderArea(areaName);
                    sender.sendMessage(new TextComponentString("Now showing area " + areaName));
                }
                else
                    sender.sendMessage(new TextComponentString("Area " + areaName + " does not exist"));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if(args.length == 1)
            return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
        else
            return Collections.emptyList();
    }
}
