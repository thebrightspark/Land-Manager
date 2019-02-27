package brightspark.landmanager.data.areas;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.message.MessageUpdateCapability;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityAreasImpl implements CapabilityAreas
{
    private Map<String, Area> areas = new HashMap<>();

    public CapabilityAreasImpl() {}

    @Override
    public boolean hasArea(String areaName)
    {
        return areas.containsKey(areaName);
    }

    @Override
    public Area getArea(String areaName)
    {
        return areas.get(areaName);
    }

    @Override
    public AddAreaResult addArea(Area area)
    {
        if(!LandManager.AREA_NAME.matcher(area.getName()).matches())
            return AddAreaResult.INVALID_NAME;
        if(areas.keySet().contains(area.getName()))
            return AddAreaResult.NAME_EXISTS;
        for(Area a : areas.values())
            if(area.intersects(a))
                return AddAreaResult.AREA_INTERSECTS;
        areas.put(area.getName(), area);
        dataChanged();
        return AddAreaResult.SUCCESS;
    }

    @Override
    public boolean removeArea(String areaName)
    {
        boolean result = areas.remove(areaName) != null;
        if(result)
            dataChanged();
        return result;
    }

    @Override
    public boolean setAllocation(String areaName, UUID playerUuid)
    {
        Area area = getArea(areaName);
        if(area != null)
        {
            area.setAllocatedPlayer(playerUuid);
            dataChanged();
        }
        return area != null;
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
            if(area.intersects(pos))
                nearbyAreas.add(area);
            else
            {
                BlockPos min = area.getMinPos();
                BlockPos max = area.getMaxPos();
                int closestX = MathHelper.clamp(pos.getX(), min.getX(), max.getX());
                int closestY = MathHelper.clamp(pos.getY(), min.getY(), max.getY());
                int closestZ = MathHelper.clamp(pos.getZ(), min.getZ(), max.getZ());

                if(new BlockPos(closestX, closestY, closestZ).getDistance(pos.getX(), pos.getY(), pos.getZ()) <= LMConfig.client.showAllRadius)
                    nearbyAreas.add(area);
            }
        });
        return nearbyAreas;
    }

    @Override
    public Area intersectingArea(BlockPos pos)
    {
        return areas.values().stream().filter(area -> area.intersects(pos)).findFirst().orElse(null);
    }

    @Override
    public Set<Area> intersectingAreas(BlockPos pos)
    {
        return areas.values().stream().filter(area -> area.intersects(pos)).collect(Collectors.toSet());
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
        areas.clear();
        NBTTagList tagList = nbt.getTagList("areas", Constants.NBT.TAG_COMPOUND);
        tagList.forEach(tag -> {
            Area area = new Area((NBTTagCompound) tag);
            areas.put(area.getName(), area);
        });
    }
}
