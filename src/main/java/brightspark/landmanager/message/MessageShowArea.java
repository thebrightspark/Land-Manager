package brightspark.landmanager.message;

import brightspark.landmanager.handler.ClientEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageShowArea implements IMessage
{
    public boolean toggleShowAll = false;
    public String showArea = null;

    public MessageShowArea() {}

    public MessageShowArea(String showArea)
    {
        toggleShowAll = showArea == null;
        this.showArea = showArea;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        toggleShowAll = buf.readBoolean();
        if(!toggleShowAll) showArea = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(toggleShowAll);
        if(!toggleShowAll) ByteBufUtils.writeUTF8String(buf, showArea);
    }

    public static class Handler implements IMessageHandler<MessageShowArea, IMessage>
    {
        @Override
        public IMessage onMessage(MessageShowArea message, MessageContext ctx)
        {
            if(message.toggleShowAll)
                ClientEventHandler.toggleRenderAll();
            else
                ClientEventHandler.setRenderArea(message.showArea);
            return null;
        }
    }
}
