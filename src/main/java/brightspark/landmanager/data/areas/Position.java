package brightspark.landmanager.data.areas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class Position implements INBTSerializable<NBTTagCompound>
{
    public int dimensionId;
    public BlockPos position;

    public Position(int dimensionId, BlockPos position)
    {
        this.dimensionId = dimensionId;
        this.position = position;
    }

    public Position(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("dimension", dimensionId);
        nbt.setLong("position", position.toLong());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        dimensionId = nbt.getInteger("dimension");
        position = BlockPos.fromLong(nbt.getLong("position"));
    }
}
