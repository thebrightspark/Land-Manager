package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;

//lm setcost <areaName> <cost>
//OR
//lm op setcost <areaName> <cost>
public class CommandSetCost extends LMCommand
{
	@Override
	public String getName()
	{
		return "setcost";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return LMConfig.permissions.setCost ?  "lm.command.setcost.usage" : "lm.command.setcost.usage.op";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length != 2)
			throwWrongUsage(sender);

		Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, args[0]);
		checkCanEditArea(server, sender, pair.getRight());

		//TODO: Set area cost
	}
}
