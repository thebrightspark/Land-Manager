package brightspark.landmanager.data.requests;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.INBTSerializable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Request implements INBTSerializable<NBTTagCompound> {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private int id;
	private String areaName;
	private UUID playerUuid;
	private long timestamp;

	public Request(int id, String areaName, UUID playerUuid) {
		this.id = id;
		this.areaName = areaName;
		this.playerUuid = playerUuid;
		timestamp = System.currentTimeMillis();
	}

	public Request(NBTTagCompound nbt) {
		deserializeNBT(nbt);
	}

	public int getId() {
		return id;
	}

	public String getAreaName() {
		return areaName;
	}

	public UUID getPlayerUuid() {
		return playerUuid;
	}

	public String getPlayerName(MinecraftServer server) {
		GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(playerUuid);
		return profile == null ? null : profile.getName();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getDate() {
		return DATE_FORMAT.format(new Date(timestamp));
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("id", id);
		nbt.setString("areaName", areaName);
		nbt.setUniqueId("player", playerUuid);
		nbt.setLong("timestamp", timestamp);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		id = nbt.getInteger("id");
		areaName = nbt.getString("areaName");
		playerUuid = nbt.getUniqueId("player");
		timestamp = nbt.getLong("timestamp");
	}
}
