package brightspark.landmanager.message;

import brightspark.landmanager.gui.GuiHome;
import brightspark.landmanager.util.HomeGuiToggleType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHomeToggleReply implements IMessage
{
	private HomeGuiToggleType type;
	private boolean state;

	public MessageHomeToggleReply() {}

	public MessageHomeToggleReply(HomeGuiToggleType type, boolean state)
	{
		this.type = type;
		this.state = state;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		type = HomeGuiToggleType.values()[buf.readByte()];
		state = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		buf.writeBoolean(state);
	}

	public static class Handler implements IMessageHandler<MessageHomeToggleReply, IMessage>
	{
		@Override
		public IMessage onMessage(MessageHomeToggleReply message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Gui gui = mc.currentScreen;
			if(!(gui instanceof GuiHome))
				return null;
			((GuiHome) gui).setToggle(message.type, message.state);
			return null;
		}
	}
}
