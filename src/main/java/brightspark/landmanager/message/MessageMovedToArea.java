package brightspark.landmanager.message;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.util.TextColour;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMovedToArea implements IMessage {
	private String name;
	private boolean member;

	public MessageMovedToArea() {
	}

	public MessageMovedToArea(String name, boolean member) {
		this.name = name == null ? "" : name;
		this.member = member;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		name = ByteBufUtils.readUTF8String(buf);
		member = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		buf.writeBoolean(member);
	}

	public static class Handler implements IMessageHandler<MessageMovedToArea, IMessage> {
		@Override
		public IMessage onMessage(MessageMovedToArea message, MessageContext ctx) {
			if (!LMConfig.client.titleOnAreaChange)
				return null;
			ITextComponent text = message.name.isEmpty() ? new TextComponentTranslation("misc.wilderness") : new TextComponentString(message.name);
			TextColour colour = message.name.isEmpty() ? LMConfig.client.titleColourWilderness : message.member ? LMConfig.client.titleColourAreaMember : LMConfig.client.titleColourAreaOutsider;
			text.getStyle().setColor(colour.colour);
			GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
			// Set area name as sub-title
			gui.displayTitle(null, text.getFormattedText(), 0, 0, 0);
			// Display empty title so that sub-title is shown
			gui.displayTitle("", null, 0, 0, 0);
			return null;
		}
	}
}
