package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.item.LMItems;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm tool
//OR
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
        return LMConfig.permissions.tool ?  "lm.command.tool.usage" : "lm.command.tool.usage.op";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return getPermissionLevel(LMConfig.permissions.tool);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        validateSenderIsPlayer(sender);

        if(!((EntityPlayer) sender).addItemStackToInventory(new ItemStack(LMItems.adminItem)))
            sender.sendMessage(new TextComponentTranslation("lm.command.tool.inventory"));
    }
}
