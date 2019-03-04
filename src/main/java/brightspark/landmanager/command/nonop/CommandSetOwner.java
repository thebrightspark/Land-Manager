package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;

//lm setowner <areaName> [playerName]
public class CommandSetOwner extends LMCommand
{
	@Override
	public String getName()
	{
		return "setowner";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length < 1)
			throwWrongUsage(sender);
		//Only OPs can set the owner to null
		if(args.length == 1 && isOP(server, sender))
			throw new CommandException("lm.command.setowner.op");

		String areaName = args[0];
		Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, areaName);
		UUID player = null;
		if(args.length > 1)
		{
			checkCanEditArea(server, sender, pair.getRight());
			player = getUuidFromPlayerName(server, argsToString(args, 1));
		}

		//Set the owner
		if(pair.getLeft().setOwner(areaName, player))
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.setowner.success"));
			LandManager.areaLog(AreaLogType.SET_OWNER, areaName, sender);
		}
		else
			throw new CommandException("lm.command.none", areaName);
	}
}
