package brightspark.landmanager.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class Area implements INBTSerializable<NBTTagCompound>
{
    private String name;
    private int dimensionId;
    private BlockPos pos1, pos2, center;
    private UUID allocatedPlayer;

    public Area(String name, int dimensionId, BlockPos position1, BlockPos position2)
    {
        this.name = name;
        this.dimensionId = dimensionId;
        pos1 = new BlockPos(
                Math.min(position1.getX(), position2.getX()),
                Math.min(position1.getY(), position2.getY()),
                Math.min(position1.getZ(), position2.getZ()));
        pos2 = new BlockPos(
                Math.max(position1.getX(), position2.getX()),
                Math.max(position1.getY(), position2.getY()),
                Math.max(position1.getZ(), position2.getZ()));
    }

    public Area(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    private static int median(int in1, int in2)
    {
        return (in1 + in2) / 2;
    }

    public String getName()
    {
        return name;
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public BlockPos getMinPos()
    {
        return pos1;
    }

    public BlockPos getMaxPos()
    {
        return pos2;
    }

    public BlockPos getCenter()
    {
        if(center == null)
        {
            center = new BlockPos(
                    median(pos1.getX(), pos2.getX()),
                    median(pos1.getY(), pos2.getY()),
                    median(pos1.getZ(), pos2.getZ()));
        }
        return center;
    }

    public UUID getAllocatedPlayer()
    {
        return allocatedPlayer;
    }

    public boolean intersects(Area area)
    {
        return new AxisAlignedBB(this.pos1, this.pos2).intersects(new AxisAlignedBB(area.pos1, area.pos2));
    }

    public boolean intersects(BlockPos pos)
    {
        return new AxisAlignedBB(this.pos1, this.pos2).contains(new Vec3d(pos).add(new Vec3d(0.5d, 0.5d, 0.5d)));
    }

    public void extendToMinMaxY()
    {
        pos1 = new BlockPos(pos1.getX(), 0, pos1.getZ());
        pos2 = new BlockPos(pos2.getX(), 255, pos2.getZ());
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", name);
        nbt.setInteger("dimension", dimensionId);
        nbt.setLong("position1", pos1.toLong());
        nbt.setLong("position2", pos2.toLong());
        if(allocatedPlayer != null)
        {
            nbt.setLong("uuid_most", allocatedPlayer.getMostSignificantBits());
            nbt.setLong("uuid_least", allocatedPlayer.getLeastSignificantBits());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        name = nbt.getString("name");
        dimensionId = nbt.getInteger("dimension");
        pos1 = BlockPos.fromLong(nbt.getLong("position1"));
        pos2 = BlockPos.fromLong(nbt.getLong("position2"));
        if(nbt.hasKey("uuid_most"))
            allocatedPlayer = new UUID(nbt.getLong("uuid_most"), nbt.getLong("uuid_least"));
    }
}
