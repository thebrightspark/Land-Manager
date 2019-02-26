package brightspark.landmanager.data.requests;

import brightspark.landmanager.LandManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class RequestsWorldSavedData extends WorldSavedData
{
	private static final String NAME = LandManager.MOD_ID + "requests";

	private int next_id = 0;
	private Map<String, Set<Request>> requestsByArea = new HashMap<>();
	private Set<Request> requests = new HashSet<>();

	public RequestsWorldSavedData()
	{
		this(NAME);
	}

	public RequestsWorldSavedData(String name)
	{
		super(name);
	}

	public static RequestsWorldSavedData get(World world)
	{
		MapStorage storage = world.getMapStorage();
		if(storage == null)
			return null;
		RequestsWorldSavedData instance = (RequestsWorldSavedData) storage.getOrLoadData(RequestsWorldSavedData.class, NAME);
		if(instance == null)
		{
			instance = new RequestsWorldSavedData();
			storage.setData(NAME, instance);
		}
		return instance;
	}

	private boolean hasRequest(String areaName, UUID playerUuid)
	{
		Set<Request> areaRequests = requestsByArea.get(areaName);
		if(areaRequests == null)
			return false;
		return areaRequests.stream().anyMatch(request ->
			request.getAreaName().equals(areaName) && request.getPlayerUuid().equals(playerUuid));
	}

	private Predicate<Request> matchById(int requestId)
	{
		return request -> request.getId() == requestId;
	}

	public Integer addRequest(String areaName, UUID playerUuid)
	{
		if(hasRequest(areaName, playerUuid))
			return null;

		Request request = new Request(next_id++, areaName, playerUuid);
		requests.add(request);
		getRequestsByArea(areaName).add(request);
		markDirty();
		return request.getId();
	}

	public boolean deleteRequest(@Nullable String areaName, int requestId)
	{
		Request request;
		if(areaName != null)
		{
			Set<Request> areaRequests = getRequestsByArea(areaName);
			request = areaRequests.stream().filter(matchById(requestId)).findFirst().orElse(null);
		}
		else
			request = requests.stream().filter(matchById(requestId)).findFirst().orElse(null);

		if(request == null)
			return false;

		Request finalRequest = request;
		Map.Entry<String, Set<Request>> entry = requestsByArea.entrySet().stream().filter(e ->
			e.getKey().equals(finalRequest.getAreaName())).findFirst().orElse(null);
		if(entry == null)
			return false;

		entry.getValue().removeIf(matchById(requestId));
		requests.removeIf(matchById(requestId));
		markDirty();
		return true;
	}

	public boolean deleteAllForArea(String areaName)
	{
		boolean success = requestsByArea.remove(areaName) != null | requests.removeIf(request -> request.getAreaName().equals(areaName));
		markDirty();
		return success;
	}

	public Request getRequestById(int id)
	{
		return requests.stream().filter(req -> req.getId() == id).findFirst().orElse(null);
	}

	public Set<Request> getRequestsByArea(String areaName)
	{
		return requestsByArea.computeIfAbsent(areaName, k -> new HashSet<>());
	}

	public Set<Request> getAllRequests()
	{
		return requests;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		next_id = nbt.getInteger("nextId");

		requests.clear();
		requestsByArea.clear();
		NBTTagList list = nbt.getTagList("list", Constants.NBT.TAG_COMPOUND);
		for(NBTBase listNbt : list)
		{
			Request request = new Request((NBTTagCompound) listNbt);
			requests.add(request);
			getRequestsByArea(request.getAreaName()).add(request);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("nextId", next_id);

		NBTTagList list = new NBTTagList();
		requests.forEach(request -> list.appendTag(request.serializeNBT()));
		nbt.setTag("list", list);

		return nbt;
	}
}
