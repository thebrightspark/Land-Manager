package brightspark.landmanager.message;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.HomeGuiToggleType;
import brightspark.landmanager.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHomeToggle implements IMessage
{
	private BlockPos pos;
	private HomeGuiToggleType type;

	public MessageHomeToggle() {}

	public MessageHomeToggle(BlockPos pos, HomeGuiToggleType type)
	{
		this.pos = pos;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		type = HomeGuiToggleType.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeByte(type.ordinal());
	}

	public static class Handler implements IMessageHandler<MessageHomeToggle, MessageHomeToggleReply>
	{
		@Override
		public MessageHomeToggleReply onMessage(MessageHomeToggle message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
			if(cap == null)
				return null;
			Area area = cap.intersectingArea(message.pos);
			boolean playerIsOp = Utils.isOp(player.world.getMinecraftServer(), player);
			if(area == null || (!area.isOwner(player.getUniqueID()) && !playerIsOp))
				return null;
			HomeGuiToggleType type = message.type;
			switch(type)
			{
				case INTERACTIONS:
					if(!playerIsOp && !LMConfig.permissions.interactions)
						return null;
					area.toggleInteractions();
					cap.dataChanged();
					return new MessageHomeToggleReply(type, area.canInteract());
				case PASSIVE_SPAWNS:
					if(!playerIsOp && !LMConfig.permissions.passiveSpawning)
						return null;
					area.togglePassiveSpawning();
					cap.dataChanged();
					return new MessageHomeToggleReply(type, area.canPassiveSpawn());
				case HOSTILE_SPAWNS:
					if(!playerIsOp && !LMConfig.permissions.hostileSpawning)
						return null;
					area.toggleHostileSpawning();
					cap.dataChanged();
					return new MessageHomeToggleReply(type, area.canHostileSpawn());
				case EXPLOSIONS:
					if(!playerIsOp && !LMConfig.permissions.explosions)
						return null;
					area.toggleExplosions();
					cap.dataChanged();
					return new MessageHomeToggleReply(type, area.canExplosionsCauseDamage());
			}
			return null;
		}
	}
}
