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

//lm explosions <areaName>
//OR
//lm op explosions <areaName>
public class CommandExplosions extends LMCommandArea {
	@Override
	public String getName() {
		return "explosions";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return LMConfig.permissions.explosions ? "lm.command.explosions.usage" : "lm.command.explosions.usage.op";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return getPermissionLevel(LMConfig.permissions.explosions);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException {
		checkCanEditArea(server, sender, area);

		area.toggleExplosions();
		cap.dataChanged(area, AreaUpdateType.CHANGE);
		sender.sendMessage(new TextComponentTranslation("lm.command.explosions.success", area.canExplosionsCauseDamage(), area.getName()));
	}
}
