package brightspark.landmanager.message;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.util.AreaChangeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageChatLog implements IMessage
{
	public Long timestamp;
	public AreaChangeType type;
	public String areaName;
	public String playerName;

	public MessageChatLog() {}

	public MessageChatLog(Long timestamp, AreaChangeType type, String areaName, String playerName)
	{
		this.timestamp = timestamp;
		this.type = type;
		this.areaName = areaName;
		this.playerName = playerName;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		timestamp = buf.readLong();
		type = AreaChangeType.values()[buf.readByte()];
		areaName = ByteBufUtils.readUTF8String(buf);
		playerName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(timestamp);
		buf.writeByte(type.ordinal());
		ByteBufUtils.writeUTF8String(buf, areaName);
		ByteBufUtils.writeUTF8String(buf, playerName);
	}

	public static class Handler implements IMessageHandler<MessageChatLog, IMessage>
	{
		private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		@Override
		public IMessage onMessage(MessageChatLog message, MessageContext ctx)
		{
			if(LMConfig.client.showChatLogs)
			{
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() ->
				{
					ITextComponent text = new TextComponentString(DATE_FORMAT.format(new Date(message.timestamp)));
					text.getStyle().setColor(TextFormatting.GRAY);
					text.appendText(" ")
						.appendSibling(new TextComponentTranslation(message.type.unlocalisedName))
						.appendText(": ")
						.appendText(message.areaName)
						.appendText(" -> ")
						.appendText(message.playerName);
					mc.player.sendMessage(text);
				});
			}
			return null;
		}
	}
}
