package brightspark.landmanager.data;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.message.MessageUpdateCapability;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class CapabilityAreasImpl implements CapabilityAreas
{
    private static final String NAME = LandManager.MOD_ID + "_areas";

    private Map<String, Area> areas = new HashMap<>();

    public CapabilityAreasImpl() {}

    @Override
    public Area getArea(String areaName)
    {
        return areas.get(areaName);
    }

    @Override
    public AddAreaResult addArea(Area area)
    {
        if(areas.keySet().contains(area.getName()))
            return AddAreaResult.NAME_EXISTS;
        for(Area a : areas.values())
            if(area.intersects(a))
                return AddAreaResult.AREA_INTERSECTS;
        areas.put(area.getName(), area);
        return AddAreaResult.SUCCESS;
    }

    @Override
    public boolean removeArea(String areaName)
    {
        return areas.remove(areaName) != null;
    }

    @Override
    public boolean setAllocation(String areaName, UUID playerUuid)
    {
        Area area = getArea(areaName);
        if(area != null) area.setAllocatedPlayer(playerUuid);
        return area != null;
    }

    @Override
    public boolean clearAllocation(String areaName)
    {
        return setAllocation(areaName, null);
    }

    @Override
    public List<Area> getAllAreas()
    {
        return Lists.newArrayList(areas.values());
    }

    @Override
    public List<String> getAllAreaNames()
    {
        return Lists.newArrayList(areas.keySet());
    }

    @Override
    public Set<Area> getNearbyAreas(BlockPos pos)
    {
        Set<Area> nearbyAreas = new HashSet<>();
        areas.values().forEach(area -> {
            if(area.getCenter().getDistance(pos.getX(), pos.getY(), pos.getZ()) <= 16)
                nearbyAreas.add(area);
        });
        return nearbyAreas;
    }

    @Override
    public boolean isIntersectingArea(BlockPos pos)
    {
        for(Area area : areas.values())
            if(area.intersects(pos))
                return true;
        return false;
    }

    @Override
    public void dataChanged()
    {
        LandManager.NETWORK.sendToAll(new MessageUpdateCapability(serializeNBT()));
    }

    @Override
    public void sendDataToPlayer(EntityPlayerMP player)
    {
        LandManager.NETWORK.sendTo(new MessageUpdateCapability(serializeNBT()), player);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        areas.values().forEach(area -> tagList.appendTag(area.serializeNBT()));
        nbt.setTag("areas", tagList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("areas", Constants.NBT.TAG_COMPOUND);
        tagList.forEach(tag -> {
            Area area = new Area((NBTTagCompound) tag);
            areas.put(area.getName(), area);
        });
    }
}
