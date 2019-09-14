package brightspark.landmanager.util;

import brightspark.landmanager.data.areas.Area;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListOpsEntry;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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

	public static boolean canPlayerEditArea(Area area, EntityPlayer player, MinecraftServer server)
	{
		if(area == null || player == null || server == null)
			return false;
		return area.isOwner(player.getUniqueID()) || isOp(server, player);
	}

	public static List<String> getAllPlayerNames(MinecraftServer server)
	{
		List<String> players = new LinkedList<>();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();
		for(String name : profileCache.getUsernames())
		{
			GameProfile profile = profileCache.getGameProfileForUsername(name);
			if(profile != null)
				players.add(profile.getName());
		}
		players.sort(Comparator.naturalOrder());
		return players;
	}

	public static String getPlayerName(MinecraftServer server, UUID uuid)
	{
		GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
		return profile == null ? null : profile.getName();
	}

	public static boolean checkCommandPermission(CommandBase command, MinecraftServer server, ICommandSender sender)
	{
		if(server.isSinglePlayer())
			return true;
		int requiredPermLevel = command.getRequiredPermissionLevel();
		if(sender instanceof EntityPlayerMP)
			return server.getPlayerList().getOppedPlayers().getPermissionLevel(((EntityPlayerMP) sender).getGameProfile()) >= requiredPermLevel;
		return sender.canUseCommand(requiredPermLevel, command.getName());
	}
}
