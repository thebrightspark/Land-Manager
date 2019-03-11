package brightspark.landmanager.command.op;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.requests.Request;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;

//lm op disapprove <requestId>
public class CommandDisapprove extends LMCommand
{
	@Override
	public String getName()
	{
		return "disapprove";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.disapprove.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length != 1)
			throwWrongUsage(sender);

		Integer id = parseIntWithDefault(args[0]);
		if(id == null)
			throwWrongUsage(sender);

		RequestsWorldSavedData requests = RequestsWorldSavedData.get(server.getEntityWorld());
		if(requests == null)
			throw new CommandException("lm.command.reqdata");

		//noinspection ConstantConditions
		Request request = requests.getRequestById(id);
		if(request == null)
			throw new CommandException("lm.command.disapprove.noRequest", id);

		//Disapprove the claim request
		String areaName = request.getAreaName();
		Pair<CapabilityAreas, Area> pair = getAreaAndCapNoException(server, areaName);
		if(pair == null)
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.disapprove.noArea", areaName));
			requests.deleteAllForArea(areaName);
			return;
		}
		requests.deleteRequest(areaName, id);
		sender.sendMessage(new TextComponentTranslation("lm.command.disapprove.success", id, getPlayerNameFromUuid(server, request.getPlayerUuid()), areaName));

		//Notify the player if they're online
		EntityPlayerMP player = getPlayerFromUuid(server, request.getPlayerUuid());
		if(player != null)
		{
			TextComponentTranslation textComp = new TextComponentTranslation("lm.command.disapprove.playerMessage", areaName, sender.getDisplayName());
			textComp.getStyle().setColor(TextFormatting.RED);
			player.sendMessage(textComp);
		}
	}
}
