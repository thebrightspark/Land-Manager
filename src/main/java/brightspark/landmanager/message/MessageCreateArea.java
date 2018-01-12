package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
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

    public static class Handler implements IMessageHandler<MessageCreateArea, MessageCreateAreaReply>
    {
        public Handler() {}

        @Override
        public MessageCreateAreaReply onMessage(MessageCreateArea message, MessageContext ctx)
        {
            //TODO: Test area creation... seems that intersection isn't working as expected
            EntityPlayer player = ctx.getServerHandler().player;
            CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
            return cap == null ? null : new MessageCreateAreaReply(cap.addArea(message.area));
        }
    }
}
