package brightspark.landmanager.data.areas;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.message.*;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityAreasImpl implements CapabilityAreas
{
    private Map<String, Area> areas = new HashMap<>();
    private Map<UUID, Integer> numAreasPerPlayer = new HashMap<>();

    public CapabilityAreasImpl() {}

    private boolean isClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

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
    public boolean addArea(Area area)
    {
        if(hasArea(area.getName()))
            return false;
        areas.put(area.getName(), area);
        dataChanged(area, AreaUpdateType.ADD);
        return true;
    }

    @Override
    public boolean removeArea(String areaName)
    {
        Area areaRemoved = areas.remove(areaName);
        boolean removed = areaRemoved != null;
        if(removed)
            dataChanged(areaRemoved, AreaUpdateType.DELETE);
        return removed;
    }

    @Override
    public void updateArea(Area area)
    {
        areas.put(area.getName(), area);
    }

    @Override
    public boolean renameArea(String oldName, String newName)
    {
        Area area = areas.remove(oldName);
        if (area == null)
            return false;
        area.setName(newName);
        areas.put(newName, area);
        if(isClientSide())
            LandManager.NETWORK.sendToAll(new MessageAreaRename(oldName, newName));
        return true;
    }

    @Override
    public boolean setOwner(String areaName, UUID playerUuid)
    {
        Area area = getArea(areaName);
        if(area != null)
        {
            area.setOwner(playerUuid);
            dataChanged(area, AreaUpdateType.CHANGE);
            return true;
        }
        return false;
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
    public boolean intersectsAnArea(Area area)
    {
        return areas.values().stream().anyMatch(area::intersects);
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
	public int getNumAreasJoined(UUID playerUuid)
	{
		return numAreasPerPlayer.computeIfAbsent(playerUuid, k -> 0);
	}

    @Override
    public boolean canJoinArea(UUID playerUuid)
    {
        return LMConfig.maxAreasCanOwn < 0 || getNumAreasJoined(playerUuid) < LMConfig.maxAreasCanOwn;
    }

	@Override
    public void increasePlayerAreasNum(UUID playerUuid)
    {
        numAreasPerPlayer.compute(playerUuid, (uuid, num) -> num == null ? 1 : num + 1 > LMConfig.maxAreasCanOwn ? num : ++num);
    }

    @Override
    public void decreasePlayerAreasNum(UUID playerUuid)
    {
        numAreasPerPlayer.compute(playerUuid, (uuid, num) -> num == null ? 0 : num > 0 ? --num : num);
    }

    @Override
    public void dataChanged()
    {
        if(isClientSide()) return;
        LandManager.NETWORK.sendToAll(new MessageUpdateCapability(serializeNBT()));
    }

	@Override
	public void dataChanged(Area area, AreaUpdateType type)
	{
        if(isClientSide()) return;
	    switch(type)
        {
            case DELETE:
                LandManager.NETWORK.sendToAll(new MessageAreaDelete(area.getName()));
                break;
            case ADD:
                LandManager.NETWORK.sendToAll(new MessageAreaAdd(area));
                break;
            case CHANGE:
                LandManager.NETWORK.sendToAll(new MessageAreaChange(area));
        }
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
        numAreasPerPlayer.clear();
        NBTTagList tagList = nbt.getTagList("areas", Constants.NBT.TAG_COMPOUND);
        tagList.forEach(tag -> {
            Area area = new Area((NBTTagCompound) tag);
            areas.put(area.getName(), area);
            if(area.getOwner() != null)
            	increasePlayerAreasNum(area.getOwner());
            area.getMembers().forEach(this::increasePlayerAreasNum);
        });
    }
}
