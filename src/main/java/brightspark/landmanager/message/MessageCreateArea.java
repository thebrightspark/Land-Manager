package brightspark.landmanager.message;

import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.AreasWorldSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCreateArea implements IMessage
{
    public Area area;

    public MessageCreateArea() {}

    public MessageCreateArea(Area area)
    {
        this.area = area;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        area = new Area(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, area.serializeNBT());
    }

    public class Handler implements IMessageHandler<MessageCreateArea, MessageCreateAreaReply>
    {
        @Override
        public MessageCreateAreaReply onMessage(MessageCreateArea message, MessageContext ctx)
        {
            EntityPlayer player = ctx.getServerHandler().player;
            AreasWorldSavedData wsd = AreasWorldSavedData.get(player.world);
            return wsd == null ? null : new MessageCreateAreaReply(wsd.addArea(message.area));
        }
    }
}
