package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.item.LMItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandLandManagerOp extends LMCommand
{
    private static final String[] argNames = new String[] {"allocate", "clearAllocation", "delete", "passives", "hostiles", "explosions", "interactions", "tool"};

    @Override
    public String getName()
    {
        return "lmop";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "\nlmop delete <areaName>\n" +
                "lmop allocate <playerName> <areaName>\n" +
                "lmop clearAllocation <areaName>\n" +
                "lmop passives <areaName>\n" +
                "lmop hostiles <areaName>\n" +
                "lmop explosions <areaName>\n" +
                "lmop interactions <areaName>\n" +
                "lmop tool";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        String command = args[0].toLowerCase();

        String areaName = null;
        CapabilityAreas cap = null;
        //Only get the area name and capability from the arguments if necessary for the command
        if(!command.equals("tool"))
        {
            areaName = argsToString(args, command.equals("allocate") ? 2 : 1);
            if(areaName.isEmpty()) areaName = null;
            if(areaName != null) cap = getWorldCapWithArea(server, areaName);
            if(cap == null)
            {
                sender.sendMessage(new TextComponentTranslation("message.command.none", areaName));
                return;
            }
        }

        switch(command)
        {
            case "delete": //lmop delete <areaName>
                if(cap.removeArea(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.delete.deleted", areaName));
                    LandManager.areaLog(AreaLogType.DELETE, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.delete.failed", areaName));
                break;
            case "allocate": //lmop allocate <playerName> <areaName>
                UUID uuid = null;
                GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
                if(profile != null) uuid = profile.getId();
                if(uuid == null)
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.noplayer", args[1]));
                    return;
                }
                if(cap.setAllocation(areaName, uuid))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.success", areaName, profile.getName()));
                    LandManager.areaLog(AreaLogType.ALLOCATE, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.failed", areaName, profile.getName()));
                break;
            case "clearallocation": //lmop clearAllocation <areaName>
                if(cap.clearAllocation(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.clear.cleared", areaName));
                    LandManager.areaLog(AreaLogType.CLEAR_ALLOCATION, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.clear.failed", areaName));
                break;
            case "passives": //lmop passives <areaName>
                if(cap.togglePassives(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.passives.success", cap.getArea(areaName).canPassiveSpawn(), areaName));
                    LandManager.areaLog(AreaLogType.SET_PASSIVES, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.passives.failed", areaName));
                break;
            case "hostiles": //lmop hostiles <areaName>
                if(cap.toggleHostiles(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.hostiles.success", cap.getArea(areaName).canHostileSpawn(), areaName));
                    LandManager.areaLog(AreaLogType.SET_HOSTILES, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.hostiles.failed", areaName));
                break;
            case "explosions": //lmop explosions <areaName>
                if(cap.toggleExplosions(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.explosions.success", cap.getArea(areaName).canExplosionsCauseDamage(), areaName));
                    LandManager.areaLog(AreaLogType.SET_EXPLOSIONS, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.explosions.failed", areaName));
                break;
            case "interactions": //lmop interactions <areaName>
                if(cap.toggleInteract(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.interactions.success", cap.getArea(areaName).canInteract(), areaName));
                    LandManager.areaLog(AreaLogType.SET_EXPLOSIONS, areaName, sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.interactions.failed", areaName));
                break;
            case "tool": //lmop tool
                if(!(sender instanceof EntityPlayer))
                    sender.sendMessage(new TextComponentTranslation("message.command.tool.player"));
                else if(!((EntityPlayer) sender).addItemStackToInventory(new ItemStack(LMItems.adminItem)))
                    sender.sendMessage(new TextComponentTranslation("message.command.tool.inventory"));
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        switch(args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, argNames);
            case 2:
                switch(args[0])
                {
                    case "allocate":
                        return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
                    case "clearAllocation":
                    case "delete":
                    case "passives":
                    case "hostiles":
                    case "explosions":
                    case "interactions":
                        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                    default:
                        return Collections.emptyList();
                }
            case 3:
                switch(args[0])
                {
                    case "allocate":
                        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                    default:
                        return Collections.emptyList();
                }
            default:
                return Collections.emptyList();
        }
    }
}
