package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.HomeGuiActionType;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageHomeAction implements IMessage
{
	private BlockPos pos;
	private HomeGuiActionType type;
	private UUID uuid;

	public MessageHomeAction() {}

	public MessageHomeAction(BlockPos pos, HomeGuiActionType type, UUID uuid)
	{
		this.pos = pos;
		this.type = type;
		this.uuid = uuid;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		type = HomeGuiActionType.values()[buf.readByte()];
		long most = buf.readLong();
		long least = buf.readLong();
		uuid = new UUID(most, least);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeByte(type.ordinal());
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
	}

	public static class Handler implements IMessageHandler<MessageHomeAction, MessageHomeActionReply>
	{
		@Override
		public MessageHomeActionReply onMessage(MessageHomeAction message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
			if(cap == null)
				return null;
			Area area = cap.intersectingArea(message.pos);
			if(area == null || !area.isOwner(player.getUniqueID()))
				return null;
			UUID uuid = message.uuid;
			GameProfile profile = player.world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(uuid);
			if(profile == null)
				return null;

			boolean changed = true;
			switch(message.type)
			{
				case ADD:
					changed = area.addMember(uuid);
					break;
				case KICK:
					changed = area.removeMember(uuid);
					break;
				case PASS:
					area.setOwner(uuid);
			}
			return changed ? new MessageHomeActionReply(message.type, uuid, profile.getName()) : null;
		}
	}
}
