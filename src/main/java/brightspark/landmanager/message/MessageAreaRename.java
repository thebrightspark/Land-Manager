package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.CapabilityAreas;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAreaRename implements IMessage {
	private String oldName, newName;

	public MessageAreaRename() {
	}

	public MessageAreaRename(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		oldName = ByteBufUtils.readUTF8String(buf);
		newName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, oldName);
		ByteBufUtils.writeUTF8String(buf, newName);
	}

	public static class Handler implements IMessageHandler<MessageAreaRename, IMessage> {
		@Override
		public IMessage onMessage(MessageAreaRename message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() ->
			{
				World world = Minecraft.getMinecraft().world;
				CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
				if (cap != null)
					cap.renameArea(message.oldName, message.newName);
			});
			return null;
		}
	}
}
