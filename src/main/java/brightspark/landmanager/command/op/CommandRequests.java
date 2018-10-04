package brightspark.landmanager.command.op;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.requests.Request;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//TODO: Need to test this!
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
		int page = args.length > 0 ? parseIntWithDefault(args[0], Integer.MIN_VALUE) : Integer.MIN_VALUE;

		String areaName = null;
		if(page == Integer.MIN_VALUE)
		{
			if(args.length > 0)
				areaName = argsToString(args, 0);
		}
		else if(args.length > 1)
			areaName = argsToString(args, 1);

		page = Math.max(0, page);

		CapabilityAreas cap = getWorldCapWithArea(server, areaName);
		Area area = cap.getArea(areaName);
		RequestsWorldSavedData reqCap = RequestsWorldSavedData.get(server.getEntityWorld());
		if(reqCap == null)
			throw new CommandException("lm.command.reqdata");

		Set<Request> requests;
		if(area == null)
			requests = reqCap.getAllRequests();
		else
			requests = reqCap.getRequestsByArea(area.getName());

		List<Request> requestsList = new LinkedList<>(requests);
		requestsList.sort(Comparator.comparing(Request::getAreaName));

		String finalAreaName = areaName == null ? "" : " " + areaName;

		sender.sendMessage(createListMessage(sender, requestsList, req ->
				String.format("%s: %s -> %s [%s]", req.getId(), req.getPlayerName(server), req.getAreaName(), req.getDate()),
			page, "lm.command.requests.title", pageNum -> "/lm op requests " + pageNum + finalAreaName));
	}
}
