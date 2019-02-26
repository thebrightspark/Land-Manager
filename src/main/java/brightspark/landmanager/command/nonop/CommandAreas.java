package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

//lm areas [page] [areaNameRegex]
public class CommandAreas extends LMCommand
{
	@Override
	public String getName()
	{
		return "areas";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.areas.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		//Get page num from args if provided
		int page = args.length > 0 ? parseIntWithDefault(args[0], Integer.MIN_VALUE) : Integer.MIN_VALUE;

		//Get area name from args if provided
		String areaName = null;
		if(page == Integer.MIN_VALUE)
		{
			if(args.length > 0)
				areaName = argsToString(args, 0);
		}
		else if(args.length > 1)
			areaName = argsToString(args, 1);

		page = Math.max(0, page);

		//Show list of all areas in pages
		List<Area> areas = areaName == null ?
			getAllAreas(server) :
			getAreasByNameRegex(server, areaName);
		if(areas.isEmpty())
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.areas.none"));
			return;
		}

		//Area name to be used for prev/next page button
		String finalAreaName = areaName == null ? "" : " " + areaName;

		//Send message back to sender
		sender.sendMessage(createListMessage(sender, areas, area ->
		{
			String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
			if(playerName == null)
				return "  " + area.getName();
			else
				return String.format("  %s -> %s", area.getName(), playerName);
		}, page, "lm.command.areas.title", pageNum -> "/lm areas " + pageNum + finalAreaName));
	}
}
