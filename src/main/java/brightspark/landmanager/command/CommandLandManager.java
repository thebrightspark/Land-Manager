package brightspark.landmanager.command;

import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
import brightspark.landmanager.item.LMItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;

public class CommandLandManager extends LMCommand
{
    @Override
    public String getName()
    {
        return "landmanager";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("lm");
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm areas [page]\n" +
                "lm area <areaName>\n" +
                "lm delete <areaName>\n" +
                "lm allocate <playerName> <areaName>\n" +
                "lm clearAllocation <areaName>\n" +
                "lm tool\n";
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
        if(command.equals("delete") || command.equals("allocate") || command.equals("clearAllocation") || command.equals("area"))
        {
            areaName = argsToString(args, command.equals("allocate") ? 2 : 1);
            if(areaName.isEmpty()) areaName = null;
            if(areaName != null) cap = getWorldCapWithArea(server, areaName);
        }

        switch(command)
        {
            case "areas": //lm areas [page]
                int page = Integer.MIN_VALUE;
                if(args.length > 1)
                {
                    try
                    {
                        page = Integer.valueOf(args[1]);
                    }
                    catch(NumberFormatException e) {}
                }

                if(args.length == 1 || page != Integer.MIN_VALUE)
                {
                    //Show list of all recipes in pages
                    List<Area> areas = getAllAreas(server);
                    if(areas.size() == 0)
                    {
                        sender.sendMessage(new TextComponentString("No areas found"));
                        return;
                    }

                    if(page < 0) page = 0;
                    int numAreas = areas.size();
                    int areasPerPage = 9;
                    int pageMax = numAreas / areasPerPage;
                    //We reduce the given page number by 1, because we calculate starting from page 0, but is shown to start from page 1.
                    if(page > 0) page--;
                    if(page * areasPerPage > numAreas) page = pageMax;

                    //Work out the range to display on the page
                    int min = page * areasPerPage;
                    int max = min + areasPerPage;
                    if(numAreas < max) max = numAreas;

                    //Create the String to send to the player
                    ITextComponent text = new TextComponentString(TextFormatting.YELLOW + "============= ");
                    TextComponentString titleText = new TextComponentString(String.format("Areas - Page %s / %s", (page + 1), (pageMax + 1)));
                    titleText.getStyle().setColor(TextFormatting.GOLD);
                    text.appendSibling(titleText);
                    text.appendText(TextFormatting.YELLOW +  " =============");
                    for(int i = min; i < max; i++)
                    {
                        Area area = areas.get(i);
                        String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
                        if(playerName == null)
                            text.appendText("\n  " + area.getName());
                        else
                            text.appendText(String.format("\n  %s -> %s", area.getName(), playerName));
                    }
                    sender.sendMessage(text);
                }
                break;
            case "area": //lm area <areaName>
                Area area = cap.getArea(areaName);
                if(area == null)
                {
                    sender.sendMessage(new TextComponentString("No area found for name " + areaName));
                    return;
                }
                String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
                if(playerName == null) playerName = "None";
                sender.sendMessage(new TextComponentString(
                        String.format(
                                "Area details:\nName: %s\nDim Id: %s\nAllocation: %s\nBlock Pos Min: %s\n Block Pos Max: %s",
                                area.getName(), area.getDimensionId(), playerName, posToString(area.getMinPos()), posToString(area.getMaxPos()))));
                break;
            case "delete": //lm delete <areaName>
                if(cap.removeArea(areaName))
                    sender.sendMessage(new TextComponentString("Deleted area " + areaName));
                else
                    sender.sendMessage(new TextComponentString("Failed to delete area " + areaName));
                break;
            case "allocate": //lm allocate <playerName> <areaName>
                UUID uuid = null;
                GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
                if(profile != null) uuid = profile.getId();
                if(uuid == null)
                {
                    sender.sendMessage(new TextComponentString("Could not find player " + args[1]));
                    return;
                }
                if(cap.setAllocation(areaName, uuid))
                break;
            case "clearAllocation": //lm clearAllocation <areaName>
                if(cap.clearAllocation(areaName))
                    sender.sendMessage(new TextComponentString("Cleared player allocation for area " + areaName));
                else
                    sender.sendMessage(new TextComponentString("Failed to remove player allocation for area " + areaName));
                break;
            case "tool": //lm tool
                if(!(sender instanceof EntityPlayer))
                    sender.sendMessage(new TextComponentString("Only players can give themselves the admin tool"));
                else if(!((EntityPlayer) sender).addItemStackToInventory(new ItemStack(LMItems.adminItem)))
                    sender.sendMessage(new TextComponentString("No room in inventory for tool"));
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
                return getListOfStringsMatchingLastWord(args, "delete", "allocate", "clearAllocation", "tool");
            case 2:
                switch(args[0])
                {
                    case "delete":
                    case "clearAllocation":
                        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                    case "allocate":
                        return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
                    default:
                        return Collections.emptyList();
                }
            case 3:
                if(args[0].equals("allocate"))
                    return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                else
                    return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }
}
