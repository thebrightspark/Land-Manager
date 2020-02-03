package brightspark.landmanager.message;

import brightspark.landmanager.gui.GuiHome;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHomeActionReplyError implements IMessage {
	private String errorMessage;
	private String[] args;

	public MessageHomeActionReplyError() {
	}

	public MessageHomeActionReplyError(String errorMessage) {
		this.errorMessage = errorMessage;
		args = new String[0];
	}

	public MessageHomeActionReplyError(String errorMessage, String... args) {
		this.errorMessage = errorMessage;
		this.args = args;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		errorMessage = ByteBufUtils.readUTF8String(buf);
		args = new String[buf.readInt()];
		for (int i = 0; i < args.length; i++)
			args[i] = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, errorMessage);
		buf.writeInt(args.length);
		for (String arg : args)
			ByteBufUtils.writeUTF8String(buf, arg);
	}

	public static class Handler implements IMessageHandler<MessageHomeActionReplyError, IMessage> {
		@Override
		public IMessage onMessage(MessageHomeActionReplyError message, MessageContext ctx) {
			Minecraft mc = Minecraft.getMinecraft();
			Gui gui = mc.currentScreen;
			if (!(gui instanceof GuiHome))
				return null;
			((GuiHome) gui).setErrorMessage(message.errorMessage, message.args);
			return null;
		}
	}
}
