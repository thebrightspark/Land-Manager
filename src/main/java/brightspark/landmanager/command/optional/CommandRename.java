package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

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
		Area area = pair.getRight();
		checkCanEditArea(server, sender, area);

		String oldName = area.getName();
		String newName = args[1];
		if(pair.getLeft().renameArea(oldName, newName))
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.success", oldName, newName));
		else
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.invalid", newName));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, getAllAreaNames(server)) : super.getTabCompletions(server, sender, args, targetPos);
	}
}
