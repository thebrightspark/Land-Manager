package brightspark.landmanager.command.optional;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.item.LMItems;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm op tool
public class CommandTool extends LMCommand
{
    @Override
    public String getName()
    {
        return "tool";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.tool.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if(!(sender instanceof EntityPlayer))
            sender.sendMessage(new TextComponentTranslation("lm.command.player"));
        else if(!((EntityPlayer) sender).addItemStackToInventory(new ItemStack(LMItems.adminItem)))
            sender.sendMessage(new TextComponentTranslation("lm.command.tool.inventory"));
    }
}
