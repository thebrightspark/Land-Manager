package brightspark.landmanager.message;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.CapabilityAreas;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateCapability implements IMessage
{
    public NBTTagCompound nbt;

    public MessageUpdateCapability() {}

    public MessageUpdateCapability(NBTTagCompound nbt)
    {
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbt);
    }

    public static class Handler implements IMessageHandler<MessageUpdateCapability, IMessage>
    {
        @Override
        public IMessage onMessage(MessageUpdateCapability message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                World world = Minecraft.getMinecraft().world;
                CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
                if(cap != null)
                    cap.deserializeNBT(message.nbt);
            });
            return null;
        }
    }
}
