package brightspark.landmanager.message;

import brightspark.landmanager.data.AddAreaResult;
import brightspark.landmanager.gui.GuiCreateArea;
import brightspark.landmanager.item.ItemAdmin;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCreateAreaReply implements IMessage
{
    public AddAreaResult result;

    public MessageCreateAreaReply() {}

    public MessageCreateAreaReply(AddAreaResult result)
    {
        this.result = result;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        result = AddAreaResult.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte((byte) result.ordinal());
    }

    public static class Handler implements IMessageHandler<MessageCreateAreaReply, IMessage>
    {
        public Handler() {}

        private static void resetItem(EntityPlayer player)
        {
            ItemStack stack = player.getHeldItemMainhand();
            if(ItemAdmin.getPos(stack) != null)
                ItemAdmin.setPos(stack, null);
        }

        @Override
        public IMessage onMessage(MessageCreateAreaReply message, MessageContext ctx)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            switch(message.result)
            {
                case SUCCESS:
                    player.sendMessage(new TextComponentString("Area added!"));
                    player.closeScreen();
                    resetItem(player);
                    break;
                case NAME_EXISTS:
                    player.sendMessage(new TextComponentString("An area with this name already exists!"));
                    GuiScreen gui = mc.currentScreen;
                    if(gui != null && gui instanceof GuiCreateArea)
                        ((GuiCreateArea) gui).clearTextField();
                    break;
                case AREA_INTERSECTS:
                    player.sendMessage(new TextComponentString("Area intersects with an existing area!"));
                    player.closeScreen();
                    resetItem(player);
            }
            return null;
        }
    }
}
