package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import brightspark.landmanager.event.AreaClaimEvent;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;

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
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException
    {
        validateSenderIsPlayer(sender);

        EntityPlayer player = (EntityPlayer) sender;

        if(LMConfig.disableClaiming)
        {
            player.sendMessage(new TextComponentTranslation("lm.command.claim.disabled"));
            return;
        }

        if(area == null)
        {
            //Get the area the player is standing in
            cap = getWorldCapForPlayer(player);
            area = getAreaStandingIn(cap, player);
        }

        String areaName = area.getName();

        if(area.getOwner() != null)
        {
            //Area already claimed
            player.sendMessage(new TextComponentTranslation("lm.command.claim.already", areaName));
            return;
        }

        //TODO: If configured to, integrate claiming with EnderPay

        if(LMConfig.permissions.claimRequest)
        {
            //Make this a "request" instead of immediately claiming
            RequestsWorldSavedData requests = getRequestsData(server);
            Integer reqId = requests.addRequest(areaName, player.getUniqueID());
            if(reqId != null)
            {
                //Request added
                player.sendMessage(new TextComponentTranslation("lm.command.claim.request.success", areaName));
                //Send chat notification to OPs
                LandManager.sendChatMessageToOPs(server, new TextComponentTranslation("lm.command.claim.request.opMessage", reqId, player.getName(), areaName), player);
            }
            else
                //Request failed
                player.sendMessage(new TextComponentTranslation("lm.command.claim.request.failed", areaName));
        }
        else if(!MinecraftForge.EVENT_BUS.post(new AreaClaimEvent(area, player)))
        {
            //Claim the area
            area.setOwner(player.getUniqueID());
            cap.dataChanged();
            player.sendMessage(new TextComponentTranslation("lm.command.claim.claimed", area.getName()));
            LandManager.areaLog(AreaLogType.CLAIM, area.getName(), player);
        }
    }
}
