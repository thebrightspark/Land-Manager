package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

//lm members <add|remove> <areaName> <playerName>
public class CommandMembers extends CommandTreeBase
{
	public CommandMembers()
	{
		addSubcommand(new CommandMembersAdd());
		addSubcommand(new CommandMembersRemove());
		addSubcommand(new CommandTreeHelp(this));
	}

	@Override
	public List<String> getAliases()
	{
		return Lists.newArrayList("member");
	}

	@Override
	public String getName()
	{
		return "members";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.members.usage";
	}

	//lm members add <areaName> <playerName>
	public static class CommandMembersAdd extends LMCommand
	{
		@Override
		public String getName()
		{
			return "add";
		}

		@Override
		public String getUsage(ICommandSender sender)
		{
			return "lm.command.members.add.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			validateSenderIsPlayer(sender);
			EntityPlayer player = (EntityPlayer) sender;

			if(args.length != 2)
				throwWrongUsage(sender);

			Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, args[0]);
			checkCanEditArea(server, sender, pair.getRight());

			UUID uuid = getUuidFromPlayerName(server, args[1]);
			if(pair.getRight().addMember(uuid))
			{
				pair.getLeft().dataChanged();
				player.sendMessage(new TextComponentTranslation("lm.command.members.add.success", player.getDisplayName(), pair.getRight().getName()));
			}
			else
				player.sendMessage(new TextComponentTranslation("lm.command.members.add.already", player.getDisplayName(), pair.getRight().getName()));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
		{
			switch(args.length)
			{
				case 1:     return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
				case 2:     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				default:    return super.getTabCompletions(server, sender, args, targetPos);
			}
		}
	}

	//lm members remove <areaName> <playerName>
	public static class CommandMembersRemove extends LMCommand
	{
		@Override
		public String getName()
		{
			return "remove";
		}

		@Override
		public String getUsage(ICommandSender sender)
		{
			return "lm.command.members.remove.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			validateSenderIsPlayer(sender);
			EntityPlayer player = (EntityPlayer) sender;

			if(args.length != 2)
				throwWrongUsage(sender);

			Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, args[0]);
			checkCanEditArea(server, sender, pair.getRight());

			UUID uuid = getUuidFromPlayerName(server, args[1]);
			if(pair.getRight().removeMember(uuid))
			{
				pair.getLeft().dataChanged();
				player.sendMessage(new TextComponentTranslation("lm.command.members.remove.success", player.getDisplayName(), pair.getRight().getName()));
			}
			else
				player.sendMessage(new TextComponentTranslation("lm.command.members.remove.already", player.getDisplayName(), pair.getRight().getName()));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
		{
			switch(args.length)
			{
				case 1:     return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
				case 2:     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				default:    return super.getTabCompletions(server, sender, args, targetPos);
			}
		}
	}
}
