package brightspark.landmanager.data.areas;

import brightspark.landmanager.LandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityAreasProvider implements ICapabilitySerializable<NBTTagCompound>
{
    private CapabilityAreas areas;

    public CapabilityAreasProvider()
    {
        areas = new CapabilityAreasImpl();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == LandManager.CAPABILITY_AREAS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return hasCapability(capability, facing) ? (T) areas : null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return areas.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        areas.deserializeNBT(nbt);
    }
}
