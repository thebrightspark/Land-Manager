package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm claim [areaName]
public class CommandClaim extends LMCommandArea
{
    public CommandClaim()
    {
        setCanHaveNoArg();
    }

    @Override
    public String getName()
    {
        return "claim";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.claim.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap)
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.player"));
            return;
        }

        if(LMConfig.disableClaiming)
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.claim.disabled"));
            return;
        }

        if(area == null)
        {
            //TODO: Claim the area the player is standing in
            sender.sendMessage(new TextComponentTranslation("lm.command.claim.standing"));
        }
        else
        {
            //Claim the specific area

            //TODO: Later and some configs to this
            //Make it so you need to request to claim? Then an OP can accept?
            //Make it so you can't use this command - only OPs can allocate?
            //Integrate with EnderPay to add a daily cost as rent?
            if(area.getAllocatedPlayer() == null)
            {
                area.setAllocatedPlayer(((EntityPlayer) sender).getUniqueID());
                cap.dataChanged();
                sender.sendMessage(new TextComponentTranslation("lm.command.claim.claimed", area.getName()));
                LandManager.areaLog(AreaLogType.CLAIM, area.getName(), sender);
            }
            else
                sender.sendMessage(new TextComponentTranslation("lm.command.claim.already", area.getName()));
        }
    }
}
