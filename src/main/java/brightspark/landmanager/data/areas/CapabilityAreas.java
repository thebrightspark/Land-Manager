package brightspark.landmanager.data.areas;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CapabilityAreas extends INBTSerializable<NBTTagCompound>
{
    boolean hasArea(String areaName);

    Area getArea(String areaName);

    boolean addArea(Area area);

    boolean removeArea(String areaName);

    void updateArea(Area area);

    boolean renameArea(String oldName, String newName);

    boolean setOwner(String areaName, UUID playerUuid);

    List<Area> getAllAreas();

    List<String> getAllAreaNames();

    Set<Area> getNearbyAreas(BlockPos pos);

    boolean intersectsAnArea(Area area);

    Area intersectingArea(BlockPos pos);

    Set<Area> intersectingAreas(BlockPos pos);

    void dataChanged();

    void dataChanged(Area area, AreaUpdateType type);

    void sendDataToPlayer(EntityPlayerMP player);

    int getNumAreasJoined(UUID playerUuid);

    boolean canJoinArea(UUID playerUuid);

    void increasePlayerAreasNum(UUID playerUuid);

    void decreasePlayerAreasNum(UUID playerUuid);
}
