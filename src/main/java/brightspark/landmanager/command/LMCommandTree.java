package brightspark.landmanager.command;

import brightspark.landmanager.util.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

public abstract class LMCommandTree extends CommandTreeBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return Utils.checkCommandPermission(this, server, sender);
	}
}
