package brightspark.landmanager.data.logs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AreaLog implements INBTSerializable<NBTTagCompound>
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public long timestamp;
    public AreaLogType type;
    public String areaName, playerName;

    public AreaLog(AreaLogType type, String areaName, String playerName)
    {
        timestamp = System.currentTimeMillis();
        this.type = type;
        this.areaName = areaName;
        this.playerName = playerName;
    }

    public AreaLog(NBTTagCompound nbt)
    {
        type = AreaLogType.valueOf(nbt.getString("type"));
        areaName = nbt.getString("area");
        playerName = nbt.getString("player");
    }

    public String getTimeString()
    {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("time", timestamp);
        nbt.setByte("type", (byte) type.ordinal());
        nbt.setString("area", areaName);
        nbt.setString("player", playerName);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        timestamp = nbt.getLong("time");
        type = AreaLogType.values()[nbt.getByte("type")];
        areaName = nbt.getString("area");
        playerName = nbt.getString("player");
    }
}
