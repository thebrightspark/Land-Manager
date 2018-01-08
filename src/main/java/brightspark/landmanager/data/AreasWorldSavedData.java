package brightspark.landmanager.data;

import brightspark.landmanager.LandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreasWorldSavedData extends WorldSavedData
{
    private static final String NAME = LandManager.MOD_ID + "_areas";

    private Map<String, Area> areas = new HashMap<>();

    public AreasWorldSavedData()
    {
        super(NAME);
    }

    public static AreasWorldSavedData get(World world)
    {
        MapStorage storage = world.getMapStorage();
        if(storage == null) return null;
        AreasWorldSavedData instance = (AreasWorldSavedData) storage.getOrLoadData(AreasWorldSavedData.class, NAME);
        if(instance == null)
        {
            instance = new AreasWorldSavedData();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    public Area getArea(String areaName)
    {
        return areas.get(areaName);
    }

    public AddAreaResult addArea(String areaName, Area area)
    {
        if(areas.keySet().contains(areaName))
            return AddAreaResult.NAME_EXISTS;
        for(Area a : areas.values())
            if(area.intersects(a))
                return AddAreaResult.AREA_INTERSECTS;
        areas.put(areaName, area);
        return AddAreaResult.SUCCESS;
    }

    public List<Area> getNearbyAreas(BlockPos pos)
    {
        List<Area> nearbyAreas = new ArrayList<>();
        areas.forEach((name, area) -> {
            if(area.getCenter().getDistance(pos.getX(), pos.getY(), pos.getZ()) <= 16)
                nearbyAreas.add(area);
        });
        return nearbyAreas;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("areas", Constants.NBT.TAG_COMPOUND);
        tagList.forEach(tag -> {
            Area area = new Area((NBTTagCompound) tag);
            areas.put(area.getName(), area);
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = new NBTTagList();
        areas.values().forEach(area -> tagList.appendTag(area.serializeNBT()));
        nbt.setTag("areas", tagList);
        return nbt;
    }
}
