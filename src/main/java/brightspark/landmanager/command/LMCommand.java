package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

abstract class LMCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    CapabilityAreas getWorldCapWithArea(MinecraftServer server, String areaName) throws CommandException
    {
        if(areaName == null) throw new WrongUsageException("No area name provided!");
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null) throw new CommandException("Failed to get areas from the world with dimension id %s", world.provider.getDimension());
            Area area = cap.getArea(areaName);
            if(area != null) return cap;
        }
        return null;
    }

    List<Area> getAllAreas(MinecraftServer server)
    {
        List<Area> areas = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null) areas.addAll(cap.getAllAreas());
        }
        areas.sort(Comparator.comparing(Area::getName));
        return areas;
    }

    List<String> getAllAreaNames(MinecraftServer server)
    {
        List<String> areaNames = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null) areaNames.addAll(cap.getAllAreaNames());
        }
        areaNames.sort(Comparator.naturalOrder());
        return areaNames;
    }

    String getPlayerNameFromUuid(MinecraftServer server, UUID uuid)
    {
        String playerName = null;
        if(uuid != null)
        {
            GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
            if(profile != null) playerName = profile.getName();
        }
        return playerName;
    }

    String posToString(BlockPos pos)
    {
        return String.format("%s, %s, %s", pos.getX(), pos.getY(), pos.getZ());
    }

    String argsToString(String[] args, int startIndex)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = startIndex; i < args.length; i++)
            sb.append(args[i]).append(" ");
        return sb.toString().trim();
    }

    ITextComponent textComponentWithColour(String text, TextFormatting colour)
    {
        return textComponentWithColour(new TextComponentString(text), colour);
    }

    ITextComponent textComponentWithColour(ITextComponent text, TextFormatting colour)
    {
        text.getStyle().setColor(colour);
        return text;
    }
}
