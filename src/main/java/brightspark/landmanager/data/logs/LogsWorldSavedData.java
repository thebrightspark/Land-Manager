package brightspark.landmanager.data.logs;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.message.MessageChatLog;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LogsWorldSavedData extends WorldSavedData
{
    private static final String NAME = LandManager.MOD_ID + "logs";
    private AreaLogList logs = new AreaLogList(LMConfig.logStorageSize);

    public LogsWorldSavedData()
    {
        super(NAME);
    }

    public LogsWorldSavedData(String name)
    {
        super(name);
    }

    public static LogsWorldSavedData get(World world)
    {
        MapStorage storage = world.getMapStorage();
        if(storage == null)
            return null;
        LogsWorldSavedData instance = (LogsWorldSavedData) storage.getOrLoadData(LogsWorldSavedData.class, NAME);
        if(instance == null)
        {
            instance = new LogsWorldSavedData();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    public void addLog(AreaLogType type, String areaName, ICommandSender sender)
    {
        AreaLog newLog = new AreaLog(type, areaName, sender.getName());
        logs.add(newLog);

        //Send chat log to all OPs
        String[] ops = sender.getEntityWorld().getMinecraftServer().getPlayerList().getOppedPlayers().getKeys();
        for(String op : ops)
        {
            EntityPlayer playerOp = sender.getEntityWorld().getPlayerEntityByName(op);
            if(playerOp != null && !playerOp.equals(sender))
                LandManager.NETWORK.sendTo(new MessageChatLog(newLog), (EntityPlayerMP) playerOp);
        }

        markDirty();
    }

    public List<AreaLog> getLogs()
    {
        return logs.getLogs();
    }

    private List<AreaLog> getLogs(Predicate<AreaLog> filter)
    {
        return logs.getLogs().stream().filter(filter).collect(Collectors.toList());
    }

    public List<AreaLog> getLogsByType(AreaLogType type)
    {
        return getLogs(log -> log.getType() == type);
    }

    public List<AreaLog> getLogsByArea(String areaName)
    {
        return getLogs(log -> log.getAreaName().equals(areaName));
    }

    public List<AreaLog> getLogsByPlayer(String playerName)
    {
        return getLogs(log -> log.getPlayerName().equals(playerName));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        logs.clear();
        NBTTagList list = nbt.getTagList("list", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> logs.add(new AreaLog((NBTTagCompound) tag)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList list = new NBTTagList();
        getLogs().forEach(log -> list.appendTag(log.serializeNBT()));
        nbt.setTag("list", list);
        return nbt;
    }
}
