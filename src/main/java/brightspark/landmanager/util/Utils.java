package brightspark.landmanager.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;

public class Utils
{
	public static boolean isOp(MinecraftServer server, ICommandSender sender)
	{
		if(!(sender instanceof EntityPlayer))
			return false;
		EntityPlayer player = (EntityPlayer) sender;
		if(player.getName().equals(server.getServerOwner()))
			return true;
		UserListOpsEntry op = server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
		return op != null;
	}
}
