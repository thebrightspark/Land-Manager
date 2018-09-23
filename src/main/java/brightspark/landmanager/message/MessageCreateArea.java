package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.AddAreaResult;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
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
            ((WorldServer) ctx.getServerHandler().player.world).addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    AddAreaResult result = AddAreaResult.INVALID;
                    Area area = message.area;
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if(area.getDimensionId() == player.dimension &&
                            area.getMinPos().getY() >= 0 &&
                            area.getMaxPos().getY() <= player.world.getHeight())
                    {
                        CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
                        if(cap != null)
                        {
                            result = cap.addArea(area);
                            if(result == AddAreaResult.SUCCESS)
                                LandManager.areaLog(AreaLogType.CREATE, area.getName(), player);
                        }
                    }

                    LandManager.NETWORK.sendTo(new MessageCreateAreaReply(area.getName(), result), player);
                }
            });
            return null;
        }
    }
}
