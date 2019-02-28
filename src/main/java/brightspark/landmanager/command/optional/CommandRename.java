package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

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
		return LMConfig.permissions.rename ?  "lm.command.rename.usage" : "lm.command.rename.usage.op";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		validateSenderIsPlayer(sender);

		if(args.length != 2)
			throwWrongUsage(sender);

		Area area = getArea(server, args[0]);
		checkCanEditArea(server, sender, area);

		String newName = args[1];
		if(area.setName(newName))
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.success", newName));
		else
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.invalid", newName));
	}
}
