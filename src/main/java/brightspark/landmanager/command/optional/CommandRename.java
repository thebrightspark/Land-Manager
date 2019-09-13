package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

//lm rename <areaName> <newAreaName>
//OR
//lm op rename <areaName> <newAreaName>
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
	public int getRequiredPermissionLevel()
	{
		return getPermissionLevel(LMConfig.permissions.rename);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		validateSenderIsPlayer(sender);

		if(args.length != 2)
			throwWrongUsage(sender);

		Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, args[0]);
		checkCanEditArea(server, sender, pair.getRight());

		String newName = args[1];
		if(pair.getRight().setName(newName))
		{
			pair.getLeft().dataChanged();
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.success", newName));
		}
		else
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.invalid", newName));
	}
}
