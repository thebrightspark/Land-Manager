package brightspark.landmanager.data.areas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Area implements INBTSerializable<NBTTagCompound>
{
    private String name;
    private int dimensionId;
    private BlockPos pos1, pos2, center;
    private UUID owner;
    private Set<UUID> members = new HashSet<>();
    private boolean
            canPassiveSpawn = true,
            canHostileSpawn = true,
            explosions = true,
            interactions = false;

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

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(UUID playerUuid)
    {
        owner = playerUuid;
    }

    public Set<UUID> getMembers()
    {
        return members;
    }

    public boolean addMember(UUID playerUuid)
    {
        return members.add(playerUuid);
    }

    public boolean removeMember(UUID playerUuid)
    {
        return members.remove(playerUuid);
    }

    public boolean isMember(UUID playerUuid)
    {
        return owner.equals(playerUuid) || members.contains(playerUuid);
    }

    public boolean canPassiveSpawn()
    {
        return canPassiveSpawn;
    }

    public void togglePassiveSpawning()
    {
        canPassiveSpawn = !canPassiveSpawn;
    }

    public boolean canHostileSpawn()
    {
        return canHostileSpawn;
    }

    public void toggleHostileSpawning()
    {
        canHostileSpawn = !canHostileSpawn;
    }

    public boolean canExplosionsCauseDamage()
    {
        return explosions;
    }

    public void toggleExplosions()
    {
        explosions = !explosions;
    }

    public boolean canInteract()
    {
        return interactions;
    }

    public void toggleInteractions()
    {
        interactions = !interactions;
    }

    public AxisAlignedBB asAABB()
    {
        Vec3d p1 = new Vec3d(pos1).add(new Vec3d(0.4d, 0.4d, 0.4d));
        Vec3d p2 = new Vec3d(pos2).add(new Vec3d(0.6d, 0.6d, 0.6d));
        return new AxisAlignedBB(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    public boolean intersects(Area area)
    {
        return asAABB().intersects(area.asAABB());
    }

    public boolean intersects(BlockPos pos)
    {
        return asAABB().contains(new Vec3d(pos).add(new Vec3d(0.5d, 0.5d, 0.5d)));
    }

    public void extendToMinMaxY(World world)
    {
        pos1 = new BlockPos(pos1.getX(), 0, pos1.getZ());
        pos2 = new BlockPos(pos2.getX(), world.getHeight(), pos2.getZ());
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", name);
        nbt.setInteger("dimension", dimensionId);
        nbt.setLong("position1", pos1.toLong());
        nbt.setLong("position2", pos2.toLong());
        if(owner != null)
            nbt.setUniqueId("player", owner);
        if(!members.isEmpty())
        {
            NBTTagList memberList = new NBTTagList();
            members.forEach(member ->
            {
                NBTTagCompound memberNbt = new NBTTagCompound();
                memberNbt.setUniqueId("uuid", member);
                memberList.appendTag(memberNbt);
            });
            nbt.setTag("members", memberList);
        }
        nbt.setBoolean("passive", canPassiveSpawn);
        nbt.setBoolean("hostile", canHostileSpawn);
        nbt.setBoolean("explosions", explosions);
        nbt.setBoolean("interact", interactions);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        name = nbt.getString("name");
        dimensionId = nbt.getInteger("dimension");
        pos1 = BlockPos.fromLong(nbt.getLong("position1"));
        pos2 = BlockPos.fromLong(nbt.getLong("position2"));
        if(nbt.hasUniqueId("player"))
            owner = nbt.getUniqueId("player");
        members.clear();
        if(nbt.hasKey("members"))
        {
            NBTTagList memberList = nbt.getTagList("members", Constants.NBT.TAG_COMPOUND);
            memberList.forEach(memberNbt -> members.add(((NBTTagCompound) memberNbt).getUniqueId("uuid")));
        }
        canPassiveSpawn = nbt.getBoolean("passive");
        canHostileSpawn = nbt.getBoolean("hostile");
        explosions = nbt.getBoolean("explosions");
        interactions = nbt.getBoolean("interact");
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj.getClass() != getClass()) return false;
        Area other = (Area) obj;

        return new EqualsBuilder()
            .append(name, other.name)
            .append(dimensionId, other.dimensionId)
            .append(pos1, other.pos1)
            .append(pos2, other.pos2)
            .isEquals();
    }
}
