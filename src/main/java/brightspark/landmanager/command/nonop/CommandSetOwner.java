package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.AreaUpdateType;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.util.Utils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
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
		return "lm.command.setowner.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length < 1)
			throwWrongUsage(sender);
		//Only OPs can set the owner to null
		if(args.length == 1 && !Utils.isOp(server, sender))
			throw new CommandException("lm.command.setowner.op");

		String areaName = args[0];
		Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, areaName);
		Area area = pair.getRight();
		String playerName = argsToString(args, 1);
		UUID playerUuid = null;
		if(args.length > 1)
		{
			checkCanEditArea(server, sender, area);
			playerUuid = getUuidFromPlayerName(server, playerName);
		}

		//Set the owner and update members
		UUID prevOwner = area.getOwner();
		area.setOwner(playerUuid);
		if(playerUuid != null)
			area.removeMember(playerUuid);
		if(prevOwner != null)
			area.addMember(prevOwner);
		pair.getLeft().dataChanged(area, AreaUpdateType.CHANGE);
		sender.sendMessage(new TextComponentTranslation("lm.command.setowner.success", areaName, playerName));
		LandManager.areaLog(AreaLogType.SET_OWNER, areaName, sender);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		switch(args.length)
		{
			case 1:     return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
			case 2:     return getListOfStringsMatchingLastWord(args, Utils.getAllPlayerNames(server));
			default:    return super.getTabCompletions(server, sender, args, targetPos);
		}
	}
}
