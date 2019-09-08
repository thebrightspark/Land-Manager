package brightspark.landmanager.message;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.logs.AreaLog;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
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
        try
        {
          log = new AreaLog(ByteBufUtils.readTag(buf));
        } catch(EncoderException e) {
          LandManager.LOGGER.warn("Unable to deserialize AreaLog's NBTCompound");
          log = null;
        }
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
                        if(log == null) return;
                        ITextComponent text = new TextComponentString(log.getTimeString());
                        text.getStyle().setColor(TextFormatting.GRAY);
                        ITextComponent typeComp = new TextComponentTranslation(log.getType().getUnlocalisedName());
                        typeComp.getStyle().setColor(log.getType().colour());
                        text.appendText("<")
                                .appendSibling(typeComp)
                                .appendText("> ")
                                .appendText(log.getAreaName())
                                .appendText(" -> ")
                                .appendText(log.getPlayerName());

                        Minecraft.getMinecraft().player.sendMessage(text);
                    }
                }
            });
            return null;
        }
    }
}
