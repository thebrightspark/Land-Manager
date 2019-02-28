package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

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
			return "lm.command.members.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			validateSenderIsPlayer(sender);
			EntityPlayer player = (EntityPlayer) sender;

			if(args.length != 2)
				throwWrongUsage(sender);

			Area area = getArea(server, args[0]);
			if(!area.isOwner(player.getUniqueID()))
			{
				player.sendMessage(new TextComponentTranslation("lm.command.members.owner", area.getName()));
				return;
			}

			UUID uuid = getUuidFromPlayerName(server, args[1]);
			if(area.addMember(uuid))
				player.sendMessage(new TextComponentTranslation("lm.command.members.add.success", player, area.getName()));
			else
				player.sendMessage(new TextComponentTranslation("lm.command.members.add.already", player, area.getName()));
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
			return "lm.command.members.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			validateSenderIsPlayer(sender);
			EntityPlayer player = (EntityPlayer) sender;

			if(args.length != 2)
				throwWrongUsage(sender);

			Area area = getArea(server, args[0]);
			if(!area.isOwner(player.getUniqueID()))
			{
				player.sendMessage(new TextComponentTranslation("lm.command.members.owner", args[0]));
				return;
			}

			UUID uuid = getUuidFromPlayerName(server, args[1]);
			if(area.removeMember(uuid))
				player.sendMessage(new TextComponentTranslation("lm.command.members.remove.success", player, area.getName()));
			else
				player.sendMessage(new TextComponentTranslation("lm.command.members.remove.already", player, area.getName()));
		}
	}
}
