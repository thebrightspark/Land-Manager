package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.HomeGuiActionType;
import brightspark.landmanager.util.Utils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageHomeActionKickOrPass implements IMessage
{
	private BlockPos pos;
	private boolean isPass;
	private UUID uuid;

	public MessageHomeActionKickOrPass() {}

	public MessageHomeActionKickOrPass(BlockPos pos, boolean isPass, UUID uuid)
	{
		this.pos = pos;
		this.isPass = isPass;
		this.uuid = uuid;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		isPass = buf.readBoolean();
		long most = buf.readLong();
		long least = buf.readLong();
		uuid = new UUID(most, least);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeBoolean(isPass);
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
	}

	public static class Handler implements IMessageHandler<MessageHomeActionKickOrPass, IMessage>
	{
		@Override
		public IMessage onMessage(MessageHomeActionKickOrPass message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
			if(cap == null)
				return null;
			Area area = cap.intersectingArea(message.pos);
			if(!Utils.canPlayerEditArea(area, player, player.world.getMinecraftServer()))
				return new MessageHomeActionReplyError("message.error.noPerm");
			UUID uuid = message.uuid;
			GameProfile profile = player.world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(uuid);
			if(profile == null)
				return new MessageHomeActionReplyError("message.error.noPlayer");

			boolean changed = true;
			if(message.isPass)
				area.setOwner(uuid);
			else
			{
				changed = area.removeMember(uuid);
				if(changed)
					cap.decreasePlayerAreasNum(uuid);
			}
			if(changed)
			{
				cap.dataChanged();
				return new MessageHomeActionReply(message.isPass ? HomeGuiActionType.PASS : HomeGuiActionType.KICK, uuid, profile.getName());
			}
			else
				return null;
		}
	}
}
