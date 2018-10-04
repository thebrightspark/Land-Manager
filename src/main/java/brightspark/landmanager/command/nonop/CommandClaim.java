package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        if(LMConfig.disableClaiming)
        {
            player.sendMessage(new TextComponentTranslation("lm.command.claim.disabled"));
            return;
        }

        if(area == null)
        {
            //Get the area the player is standing in
            BlockPos playerPos = player.getPosition();
            area = cap.intersectingArea(playerPos);
            if(area == null)
            {
                player.sendMessage(new TextComponentTranslation("lm.command.claim.none"));
                return;
            }
        }

        String areaName = area.getName();

        if(area.getAllocatedPlayer() != null)
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
        else
        {
            //Claim the area
            claimArea(player, area, cap);
        }
    }

    public static void claimArea(EntityPlayer player, Area area, CapabilityAreas cap)
    {
        area.setAllocatedPlayer(player.getUniqueID());
        cap.dataChanged();
        player.sendMessage(new TextComponentTranslation("lm.command.claim.claimed", area.getName()));
        LandManager.areaLog(AreaLogType.CLAIM, area.getName(), player);
    }
}
