package brightspark.landmanager.message;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.data.logs.AreaLog;
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

public class MessageChatLog implements IMessage
{
    public AreaLog log;

    public MessageChatLog() {}

    public MessageChatLog(AreaLog log)
    {
        this.log = log;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, log.serializeNBT());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        log = new AreaLog(ByteBufUtils.readTag(buf));
    }

    public static class Handler implements IMessageHandler<MessageChatLog, IMessage>
    {
        @Override
        public IMessage onMessage(MessageChatLog message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    if(LMConfig.client.showChatLogs)
                    {
                        AreaLog log = message.log;
                        ITextComponent text = new TextComponentString(log.getTimeString());
                        text.getStyle().setColor(TextFormatting.GRAY);
                        ITextComponent typeComp = new TextComponentTranslation(log.type.getUnlocalisedName());
                        typeComp.getStyle().setColor(log.type.colour());
                        text.appendText("<")
                                .appendSibling(typeComp)
                                .appendText("> ")
                                .appendText(log.areaName)
                                .appendText(" -> ")
                                .appendText(log.playerName);

                        Minecraft.getMinecraft().player.sendMessage(text);
                    }
                }
            });
            return null;
        }
    }
}
