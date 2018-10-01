package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.ListView;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public abstract class LMCommand extends CommandBase
{
    protected CapabilityAreas getWorldCapWithArea(MinecraftServer server, String areaName) throws CommandException
    {
        if(areaName == null)
            throw new WrongUsageException("lm.command.areaName");
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null)
                throw new CommandException("lm.command.data", world.provider.getDimension());
            if(cap.hasArea(areaName))
                return cap;
        }
        throw new CommandException("lm.command.none", areaName);
    }

    protected List<Area> getAllAreas(MinecraftServer server)
    {
        List<Area> areas = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null)
                areas.addAll(cap.getAllAreas());
        }
        areas.sort(Comparator.comparing(Area::getName));
        return areas;
    }

    protected List<String> getAllAreaNames(MinecraftServer server)
    {
        List<String> areaNames = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null)
                areaNames.addAll(cap.getAllAreaNames());
        }
        areaNames.sort(Comparator.naturalOrder());
        return areaNames;
    }

    protected String getPlayerNameFromUuid(MinecraftServer server, UUID uuid)
    {
        String playerName = null;
        if(uuid != null)
        {
            GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
            if(profile != null)
                playerName = profile.getName();
        }
        return playerName;
    }

    protected String posToString(BlockPos pos)
    {
        return String.format("%sX: %s%s, %sY: %s%s, %sZ: %s%s", TextFormatting.YELLOW, TextFormatting.RESET, pos.getX(), TextFormatting.YELLOW, TextFormatting.RESET, pos.getY(), TextFormatting.YELLOW, TextFormatting.RESET, pos.getZ());
    }

    protected String argsToString(String[] args)
    {
        return argsToString(args, 0);
    }

    protected String argsToString(String[] args, int startIndex)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = startIndex; i < args.length; i++)
            sb.append(args[i]).append(" ");
        return sb.toString().trim();
    }

    protected ITextComponent goldTextComponent(String text, Object... args)
    {
        return textComponentWithColour(TextFormatting.GOLD, text, args);
    }

    protected ITextComponent textComponentWithColour(TextFormatting colour, String text, Object... args)
    {
        ITextComponent textComponent = new TextComponentTranslation(text, args);
        textComponent.getStyle().setColor(colour);
        return textComponent;
    }

    protected <T> ListView<T> getListView(List<T> list, int page, int maxPerPage)
    {
        page = Math.max(0, page);
        int size = list.size();
        int pageMax = size / maxPerPage;
        //We reduce the given page number by 1, because we calculate starting from page 0, but is shown to start from page 1.
        if(page > 0)
            page--;
        if(page * maxPerPage > size)
            page = pageMax;
        //Work out the range to get from the list
        int min = page * maxPerPage;
        int max = min + maxPerPage;
        if(size < max)
            max = size;

        return new ListView<>(list.subList(min, max), page, pageMax);
    }

    protected TextComponentTranslation booleanToText(boolean bool)
    {
        return new TextComponentTranslation(bool ? "message.misc.true" : "message.misc.false");
    }

    protected void throwWrongUsage(ICommandSender sender) throws WrongUsageException
    {
        throw new WrongUsageException(getUsage(sender));
    }
}
