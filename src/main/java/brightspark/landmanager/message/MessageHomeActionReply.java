package brightspark.landmanager.message;

import brightspark.landmanager.gui.GuiHome;
import brightspark.landmanager.util.HomeGuiActionType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageHomeActionReply implements IMessage {
	private HomeGuiActionType type;
	private UUID uuid;
	private String name;

	public MessageHomeActionReply() {
	}

	public MessageHomeActionReply(HomeGuiActionType type, UUID uuid, String name) {
		this.type = type;
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = HomeGuiActionType.values()[buf.readByte()];
		long most = buf.readLong();
		long least = buf.readLong();
		uuid = new UUID(most, least);
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(type.ordinal());
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
	}

	public static class Handler implements IMessageHandler<MessageHomeActionReply, IMessage> {
		@Override
		public IMessage onMessage(MessageHomeActionReply message, MessageContext ctx) {
			Minecraft mc = Minecraft.getMinecraft();
			Gui gui = mc.currentScreen;
			if (!(gui instanceof GuiHome))
				return null;
			GuiHome guiHome = (GuiHome) gui;
			switch (message.type) {
				case ADD:
					guiHome.addMember(message.uuid, message.name);
					guiHome.clearInput();
					break;
				case KICK:
					guiHome.removeMember(message.uuid);
					guiHome.clearSelection();
					break;
				case PASS:
					if (!mc.player.getUniqueID().equals(message.uuid))
						mc.player.closeScreen();
			}
			return null;
		}
	}
}
