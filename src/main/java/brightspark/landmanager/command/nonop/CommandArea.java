package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//lm area <areaName>
public class CommandArea extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "area";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.area.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap)
    {
        //Get owner
        ITextComponent ownerName;
        String ownerNameString = getPlayerNameFromUuid(server, area.getOwner());
        if(ownerNameString == null)
            ownerName = new TextComponentTranslation("lm.command.area.noplayer");
        else
            ownerName = new TextComponentString(ownerNameString);

        //Get members
        ITextComponent members = null;
        Set<UUID> memberSet = area.getMembers();
        if(!memberSet.isEmpty())
        {
            List<String> names = memberSet.stream().map(uuid -> getPlayerNameFromUuid(server, uuid)).sorted().collect(Collectors.toList());
            members = new TextComponentString(String.join(", ", names));
        }

        ITextComponent text = new TextComponentString("");
        text.getStyle().setColor(TextFormatting.WHITE);
        ITextComponent areaNameComponent = new TextComponentTranslation("lm.command.area.name");
        areaNameComponent.getStyle().setColor(TextFormatting.YELLOW);
        text.appendSibling(areaNameComponent).appendText(" " + area.getName());
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.dim")).appendText(" " + area.getDimensionId());
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.owner")).appendText(" ").appendSibling(ownerName);
        if(members == null)
            text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.members.none"));
        else
            text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.members")).appendText(" ").appendSibling(members);
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.posmin")).appendText(" " + posToString(area.getMinPos()));
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.posmax")).appendText(" " + posToString(area.getMaxPos()));
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.passives")).appendText(" ").appendSibling(booleanToText(area.canPassiveSpawn()));
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.hostiles")).appendText(" ").appendSibling(booleanToText(area.canHostileSpawn()));
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.explosions")).appendText(" ").appendSibling(booleanToText(area.canHostileSpawn()));
        text.appendText("\n ").appendSibling(goldTextComponent("lm.command.area.interactions")).appendText(" ").appendSibling(booleanToText(area.canInteract()));
        sender.sendMessage(text);
    }
}
