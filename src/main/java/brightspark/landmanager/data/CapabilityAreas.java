package brightspark.landmanager.data;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CapabilityAreas extends INBTSerializable<NBTTagCompound>
{
    Area getArea(String areaName);

    AddAreaResult addArea(Area area);

    boolean removeArea(String areaName);

    boolean setAllocation(String areaName, UUID playerUuid);

    boolean clearAllocation(String areaName);

    List<Area> getAllAreas();

    List<String> getAllAreaNames();

    Set<Area> getNearbyAreas(BlockPos pos);

    boolean isIntersectingArea(BlockPos pos);

    void dataChanged();

    void sendDataToPlayer(EntityPlayerMP player);
}
