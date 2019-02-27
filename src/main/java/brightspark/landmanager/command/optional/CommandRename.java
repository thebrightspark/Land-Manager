package brightspark.landmanager.command.optional;

import brightspark.landmanager.command.LMCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandRename extends LMCommand
{
	@Override
	public String getName()
	{
		return "rename";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.rename.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		//TODO Rename areas
	}
}
