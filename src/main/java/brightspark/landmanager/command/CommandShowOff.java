package brightspark.landmanager.command;

import brightspark.landmanager.handler.ClientEventHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandShowOff extends LMCommand
{
    @Override
    public String getName()
    {
        return "lmShowOff";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lmShowOff";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentString("You need to be a player to use this command"));
            return;
        }

        ClientEventHandler.setRenderArea("");
        sender.sendMessage(new TextComponentString("Turned off showing areas"));
    }
}
