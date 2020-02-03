package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.AreaUpdateType;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm hostiles <areaName>
//OR
//lm op hostiles <areaName>
public class CommandHostiles extends LMCommandArea {
	@Override
	public String getName() {
		return "hostiles";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return LMConfig.permissions.hostileSpawning ? "lm.command.hostiles.usage" : "lm.command.hostiles.usage.op";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return getPermissionLevel(LMConfig.permissions.hostileSpawning);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException {
		checkCanEditArea(server, sender, area);

		area.toggleHostileSpawning();
		cap.dataChanged(area, AreaUpdateType.CHANGE);
		sender.sendMessage(new TextComponentTranslation("lm.command.hostiles.success", area.canHostileSpawn(), area.getName()));
	}
}
