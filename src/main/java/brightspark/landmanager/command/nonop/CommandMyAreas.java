package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.UUID;

//lm myareas [page] [areaNameRegex]
public class CommandMyAreas extends LMCommand
{
	@Override
	public String getName()
	{
		return "myareas";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.myareas.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		validateSenderIsPlayer(sender);

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
		UUID uuid = ((EntityPlayer) sender).getUniqueID();
		//Make sure we only get areas the sender is a member of
		areas.removeIf(area -> !area.isMember(uuid));
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
			String ownerName = getPlayerNameFromUuid(server, area.getOwner());
			if(ownerName == null)
				return "  " + area.getName();
			else
				return String.format("  %s -> %s", area.getName(), ownerName);
		}, page, "lm.command.myareas.title", pageNum -> "/lm areas " + pageNum + finalAreaName));
	}
}
