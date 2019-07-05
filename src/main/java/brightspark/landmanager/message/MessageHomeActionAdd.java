package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.HomeGuiActionType;
import brightspark.landmanager.util.Utils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Locale;
import java.util.UUID;

public class MessageHomeActionAdd implements IMessage
{
	private BlockPos pos;
	private String name;

	public MessageHomeActionAdd() {}

	public MessageHomeActionAdd(BlockPos pos, String name)
	{
		this.pos = pos;
		this.name = name;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		ByteBufUtils.writeUTF8String(buf, name);
	}

	public static class Handler implements IMessageHandler<MessageHomeActionAdd, IMessage>
	{
		@Override
		public IMessage onMessage(MessageHomeActionAdd message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
			if(cap == null)
				return null;
			Area area = cap.intersectingArea(message.pos);
			if(!Utils.canPlayerEditArea(area, player, player.world.getMinecraftServer()))
				return new MessageHomeActionReplyError("message.error.noPerm");
			PlayerProfileCache cache = player.world.getMinecraftServer().getPlayerProfileCache();
			if(!ArrayUtils.contains(cache.getUsernames(), message.name.toLowerCase(Locale.ROOT)))
				return new MessageHomeActionReplyError("message.error.noPlayerName", message.name);
			GameProfile profile = cache.getGameProfileForUsername(message.name);
			if(profile == null)
				return new MessageHomeActionReplyError("message.error.noPlayerName", message.name);

			UUID uuid = profile.getId();
			if(!cap.canJoinArea(uuid))
				return new MessageHomeActionReplyError("message.error.cantJoin", message.name);
			if(area.addMember(uuid))
			{
				cap.increasePlayerAreasNum(uuid);
				cap.dataChanged();
				return new MessageHomeActionReply(HomeGuiActionType.ADD, uuid, profile.getName());
			}
			return new MessageHomeActionReplyError("message.error.alreadyMember", message.name);
		}
	}
}
