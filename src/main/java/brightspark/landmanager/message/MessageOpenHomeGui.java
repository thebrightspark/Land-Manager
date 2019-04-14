package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenHomeGui implements IMessage
{
	private BlockPos pos;

	public MessageOpenHomeGui() {}

	public MessageOpenHomeGui(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
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
			return null;
		}
	}
}
