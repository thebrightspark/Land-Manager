package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.gui.GuiHome;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageOpenHomeGui implements IMessage
{
	private BlockPos pos;
	private boolean isOp;
	private List<Pair<UUID, String>> members;

	public MessageOpenHomeGui() {}

	public MessageOpenHomeGui(BlockPos pos, boolean isOp, List<Pair<UUID, String>> members)
	{
		this.pos = pos;
		this.isOp = isOp;
		this.members = members;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		isOp = buf.readBoolean();
		int membersSize = buf.readInt();
		members = new LinkedList<>();
		for(int i = 0; i < membersSize; i++)
		{
			long most = buf.readLong();
			long least = buf.readLong();
			String name = ByteBufUtils.readUTF8String(buf);
			members.add(new ImmutablePair<>(new UUID(most, least), name));
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeBoolean(isOp);
		buf.writeInt(members.size());
		members.forEach(pair ->
		{
			buf.writeLong(pair.getLeft().getMostSignificantBits());
			buf.writeLong(pair.getLeft().getLeastSignificantBits());
			ByteBufUtils.writeUTF8String(buf, pair.getRight());
		});
	}

	public static class Handler implements IMessageHandler<MessageOpenHomeGui, IMessage>
	{
		@Override
		public IMessage onMessage(MessageOpenHomeGui message, MessageContext ctx)
		{
			EntityPlayer player = Minecraft.getMinecraft().player;
			World world = player.world;
			BlockPos pos = message.pos;
			player.openGui(LandManager.INSTANCE, 1, world, pos.getX(), pos.getY(), pos.getZ());
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui instanceof GuiHome)
			{
				GuiHome guiHome = (GuiHome) gui;
				guiHome.setMembersData(message.members);
				if(message.isOp)
					guiHome.setClientIsOp();
			}
			else
				LandManager.LOGGER.warn("The home GUI wasn't open! Unable to set members data.");
			return null;
		}
	}
}
