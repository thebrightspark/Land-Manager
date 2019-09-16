package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAreaAdd implements IMessage
{
	private Area area;

	public MessageAreaAdd() {}

	public MessageAreaAdd(Area area)
	{
		this.area = area;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		area = new Area(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, area.serializeNBT());
	}

	public static class Handler implements IMessageHandler<MessageAreaAdd, IMessage>
	{
		@Override
		public IMessage onMessage(MessageAreaAdd message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() ->
			{
				World world = Minecraft.getMinecraft().world;
				CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
				if(cap != null)
					cap.addArea(message.area);
			});
			return null;
		}
	}
}
