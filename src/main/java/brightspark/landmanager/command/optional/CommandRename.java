package brightspark.landmanager.command.optional;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

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
		return "lm.command.rename.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		validateSenderIsPlayer(sender);

		if(args.length != 2)
			throwWrongUsage(sender);

		Area area = getArea(server, args[0]);
		if(!area.isOwner(((EntityPlayer) sender).getUniqueID()))
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.owner"));
			return;
		}

		String newName = args[1];
		if(area.setName(newName))
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.success", newName));
		}
		else
		{
			sender.sendMessage(new TextComponentTranslation("lm.command.rename.invalid", newName));
		}
	}
}
