package brightspark.landmanager.command.op;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.requests.Request;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import brightspark.landmanager.event.AreaClaimApprovalEvent;
import brightspark.landmanager.util.AreaChangeType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

//lm op approve <requestId>
public class CommandApprove extends LMCommand {
	@Override
	public String getName() {
		return "approve";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "lm.command.approve.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1)
			throwWrongUsage(sender);

		Integer id = parseIntWithDefault(args[0]);
		if (id == null)
			throwWrongUsage(sender);

		RequestsWorldSavedData requests = RequestsWorldSavedData.get(server.getEntityWorld());
		if (requests == null)
			throw new CommandException("lm.command.reqdata");

		//noinspection ConstantConditions
		Request request = requests.getRequestById(id);
		if (request == null)
			throw new CommandException("lm.command.approve.noRequest", id);

		String areaName = request.getAreaName();
		Pair<CapabilityAreas, Area> pair = getAreaAndCapNoException(server, areaName);
		if (pair == null) {
			sender.sendMessage(new TextComponentTranslation("lm.command.approve.noArea", areaName));
			requests.deleteAllForArea(areaName);
			return;
		}
		if (MinecraftForge.EVENT_BUS.post(new AreaClaimApprovalEvent(pair.getRight(), request, sender)))
			return;

		//Approve the claim request
		pair.getLeft().setOwner(areaName, request.getPlayerUuid());
		sender.sendMessage(new TextComponentTranslation("lm.command.approve.success", id, getPlayerNameFromUuid(server, request.getPlayerUuid()), areaName));
		//Send chat message to OPs
		LandManager.areaChange(AreaChangeType.CLAIM, areaName, sender);

		//Delete all requests for the area
		requests.deleteAllForArea(areaName);

		//Notify the player if they're online
		EntityPlayerMP player = getPlayerFromUuid(server, request.getPlayerUuid());
		if (player != null) {
			TextComponentTranslation textComp = new TextComponentTranslation("lm.command.approve.playerMessage", areaName, sender.getDisplayName());
			textComp.getStyle().setColor(TextFormatting.DARK_GREEN);
			player.sendMessage(textComp);
		}
	}
}
