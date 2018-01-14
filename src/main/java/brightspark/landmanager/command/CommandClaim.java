package brightspark.landmanager.command;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandClaim extends LMCommand
{
    @Override
    public String getName()
    {
        return "lmClaim";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lmClaim [areaName]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentString("You need to be a player to use this command"));
            return;
        }

        if(LMConfig.disableClaiming)
        {
            sender.sendMessage(new TextComponentString("Area claiming is not enabled! Ask an OP to allocate you an area."));
            return;
        }

        if(args.length == 0)
        {
            //TODO: Claim the area the player is standing in
            sender.sendMessage(new TextComponentString("Claiming the area you're standing in is still WIP. Please specify an area name."));
        }
        else
        {
            //Claim the specific area
            String areaName = argsToString(args, 0);
            if(areaName.isEmpty())
                sender.sendMessage(new TextComponentString("Invalid area name provided"));
            else
            {
                //TODO: Later and some configs to this
                //Make it so you need to request to claim? Then an OP can accept?
                //Make it so you can't use this command - only OPs can allocate?
                //Integrate with EnderPay to add a daily cost as rent?
                CapabilityAreas cap = getWorldCapWithArea(server, areaName);
                Area area = cap.getArea(areaName);
                if(area != null)
                {
                    if(area.getAllocatedPlayer() == null)
                    {
                        if(cap.setAllocation(areaName, ((EntityPlayer) sender).getUniqueID()))
                            sender.sendMessage(new TextComponentString("Area " + areaName + " claimed"));
                        else
                            sender.sendMessage(new TextComponentString("Failed to claim area " + areaName));
                    }
                    else
                        sender.sendMessage(new TextComponentString("Someone else has already claimed area " + areaName));
                }
                else
                    sender.sendMessage(new TextComponentString("Area " + areaName + " does not exist"));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if(args.length == 1)
            return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
        else
            return Collections.emptyList();
    }
}
