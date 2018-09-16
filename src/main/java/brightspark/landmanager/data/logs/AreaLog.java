package brightspark.landmanager.data.logs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AreaLog implements INBTSerializable<NBTTagCompound>
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private long timestamp;
    private AreaLogType type;
    private String areaName, playerName;

    public AreaLog(AreaLogType type, String areaName, String playerName)
    {
        timestamp = System.currentTimeMillis();
        this.type = type;
        this.areaName = areaName;
        this.playerName = playerName;
    }

    public AreaLog(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public AreaLogType getType()
    {
        return type;
    }

    public String getAreaName()
    {
        return areaName;
    }

    public String getPlayerName()
    {
        return playerName;
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
