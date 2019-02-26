package brightspark.landmanager.command.op;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.requests.Request;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

//lm op requests [page] [areaName]
public class CommandRequests extends LMCommand
{
	@Override
	public String getName()
	{
		return "requests";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.requests.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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

		//Get requests
		RequestsWorldSavedData reqCap = RequestsWorldSavedData.get(server.getEntityWorld());
		if(reqCap == null)
			throw new CommandException("lm.command.reqdata");
		List<Request> requests = StringUtils.isNullOrEmpty(areaName) ?
			new LinkedList<>(reqCap.getAllRequests()) :
			new LinkedList<>(reqCap.getRequestsByRegex(areaName));
		if(requests.isEmpty())
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.requests.none"));
			return;
		}
		requests.sort(Comparator.comparing(Request::getId));

		//Area name to be used in prev/next page button tooltip
		String finalAreaName = areaName == null ? "" : " " + areaName;

		//Send message back to sender
		sender.sendMessage(createListMessage(sender, requests, req ->
				String.format("%s: %s -> %s [%s]", req.getId(), req.getPlayerName(server), req.getAreaName(), req.getDate()),
			page, "lm.command.requests.title", pageNum -> "/lm op requests " + pageNum + finalAreaName));
	}
}
