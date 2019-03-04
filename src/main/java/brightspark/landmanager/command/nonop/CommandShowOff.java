package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.message.MessageShowArea;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm showoff
public class CommandShowOff extends LMCommand
{
    @Override
    public String getName()
    {
        return "showoff";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.showoff.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        validateSenderIsPlayer(sender);

        LandManager.NETWORK.sendTo(new MessageShowArea(""), (EntityPlayerMP) sender);
        sender.sendMessage(new TextComponentTranslation("lm.command.showoff"));
    }
}
