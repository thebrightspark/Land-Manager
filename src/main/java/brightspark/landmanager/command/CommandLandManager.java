package brightspark.landmanager.command;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.message.MessageShowArea;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandLandManager extends LMCommand
{
    @Override
    public String getName()
    {
        return "lm";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "\nlm areas [page]\n" +
                "lm area <areaName>\n" +
                "lm claim <areaName>\n" +
                "lm show [areaName]\n" +
                "lm showOff";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        String command = args[0].toLowerCase();

        if((command.equals("claim") || command.equals("show") || command.equals("showoff")) && !(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentTranslation("message.command.player"));
            return;
        }

        String areaName;
        CapabilityAreas cap;

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
                        sender.sendMessage(new TextComponentTranslation("message.command.none"));
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
                    ITextComponent titleText = new TextComponentTranslation("message.command.areas.title", (page + 1), (pageMax + 1));
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
                areaName = argsToString(args, 1);
                if(areaName.isEmpty()) areaName = null;
                if(areaName != null) cap = getWorldCapWithArea(server, areaName);
                else throw new WrongUsageException(getUsage(sender));

                Area area = cap != null ? cap.getArea(areaName) : null;
                if(area == null)
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.none", areaName));
                    return;
                }
                String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
                if(playerName == null) playerName = "message.command.area.noplayer";
                ITextComponent text = new TextComponentString("");
                text.getStyle().setColor(TextFormatting.WHITE);
                ITextComponent areaNameComponent = new TextComponentTranslation("message.command.area.name");
                areaNameComponent.getStyle().setColor(TextFormatting.YELLOW);
                text.appendSibling(areaNameComponent);
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.name", area.getName()));
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.dim", area.getDimensionId()));
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.allocation")).appendSibling(goldTextComponent(playerName));
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.posmin", posToString(area.getMinPos())));
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.posmax", posToString(area.getMaxPos())));
                text.appendText("\n ").appendSibling(goldTextComponent("message.command.area.spawning", area.getStopsEntitySpawning() ? "True" : "False"));
                sender.sendMessage(text);
                break;
            case "claim": //lm claim <areaName>
                if(LMConfig.disableClaiming)
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.claim.disabled"));
                    return;
                }

                if(args.length == 1)
                {
                    //TODO: Claim the area the player is standing in
                    sender.sendMessage(new TextComponentTranslation("message.command.claim.standing"));
                }
                else
                {
                    //Claim the specific area
                    areaName = argsToString(args, 1);
                    if(areaName.isEmpty())
                        sender.sendMessage(new TextComponentTranslation("message.command.claim.invalid"));
                    else
                    {
                        //TODO: Later and some configs to this
                        //Make it so you need to request to claim? Then an OP can accept?
                        //Make it so you can't use this command - only OPs can allocate?
                        //Integrate with EnderPay to add a daily cost as rent?
                        cap = getWorldCapWithArea(server, areaName);
                        if(cap == null)
                        {
                            sender.sendMessage(new TextComponentTranslation("message.command.none", areaName));
                            return;
                        }
                        Area areaToClaim = cap.getArea(areaName);
                        if(areaToClaim != null)
                        {
                            if(areaToClaim.getAllocatedPlayer() == null)
                            {
                                if(cap.setAllocation(areaName, ((EntityPlayer) sender).getUniqueID()))
                                {
                                    sender.sendMessage(new TextComponentTranslation("message.command.claim.claimed", areaName));
                                    LandManager.areaLog(AreaLogType.CLAIM, areaName, (EntityPlayerMP) sender);
                                }
                                else
                                    sender.sendMessage(new TextComponentTranslation("message.command.claim.failed", areaName));
                            }
                            else
                                sender.sendMessage(new TextComponentTranslation("message.command.claim.already", areaName));
                        }
                        else
                            sender.sendMessage(new TextComponentTranslation("message.command.notexist", areaName));
                    }
                }
                break;
            case "show": //lm show [areaName]
                if(args.length == 1)
                {
                    //Toggle showing all nearby areas
                    LandManager.NETWORK.sendTo(new MessageShowArea(null), (EntityPlayerMP) sender);
                }
                else
                {
                    //Show specific area
                    areaName = argsToString(args, 1);
                    if(areaName.isEmpty())
                        sender.sendMessage(new TextComponentTranslation("message.command.show.invalid"));
                    else
                    {
                        //Make sure it's a valid area name
                        List<String> areas = getAllAreaNames(server);
                        if(areas.contains(areaName))
                        {
                            LandManager.NETWORK.sendTo(new MessageShowArea(areaName), (EntityPlayerMP) sender);
                            sender.sendMessage(new TextComponentTranslation("message.command.show.showing", areaName));
                        }
                        else
                            sender.sendMessage(new TextComponentTranslation("message.command.none", areaName));
                    }
                }
                break;
            case "showoff": //lm showOff
                LandManager.NETWORK.sendTo(new MessageShowArea(""), (EntityPlayerMP) sender);
                sender.sendMessage(new TextComponentTranslation("message.command.showoff"));
                break;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if(args.length == 1)
            return getListOfStringsMatchingLastWord(args, "area", "areas", "claim", "show", "showOff");
        else if(args.length == 2 && (args[0].equals("area") || args[0].equals("claim") || args[0].equals("show")))
            return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
        return Collections.emptyList();
    }
}
