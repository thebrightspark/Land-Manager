package brightspark.landmanager.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

public interface CapabilityAreas extends INBTSerializable<NBTTagCompound>
{
    Area getArea(String areaName);

    AddAreaResult addArea(Area area);

    Set<Area> getNearbyAreas(BlockPos pos);

    boolean isIntersectingArea(BlockPos pos);

    void dataChanged();
}
