package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.event.AreaDeletedEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAreaDelete implements IMessage
{
	private String areaName;

	public MessageAreaDelete() {}

	public MessageAreaDelete(String areaName)
	{
		this.areaName = areaName;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		areaName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, areaName);
	}

	public static class Handler implements IMessageHandler<MessageAreaDelete, IMessage>
	{
		@Override
		public IMessage onMessage(MessageAreaDelete message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() ->
			{
				World world = Minecraft.getMinecraft().world;
				CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
				if(cap != null)
				{
					Area area = cap.getArea(message.areaName);
					if(area != null && cap.removeArea(message.areaName))
						MinecraftForge.EVENT_BUS.post(new AreaDeletedEvent(area));
				}
			});
			return null;
		}
	}
}
