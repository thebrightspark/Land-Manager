package brightspark.landmanager.item;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAdmin extends Item
{
    public ItemAdmin()
    {
        setUnlocalizedName("admin");
        setRegistryName("admin");
        setCreativeTab(LandManager.LM_TAB);
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return true;
    }

    public static void setPos(ItemStack stack, Position position)
    {
        if(position == null)
            stack.setTagCompound(null);
        else
            stack.setTagInfo("pos", position.serializeNBT());
    }

    public static Position getPos(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? null : new Position(tag.getCompoundTag("pos"));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        //Only work in main hand!
        if(hand == EnumHand.OFF_HAND) return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);

        ItemStack stack = player.getHeldItem(hand);
        Position position = getPos(stack);
        BlockPos actualPos = player.isSneaking() ? pos : pos.offset(side);
        if(position == null)
        {
            //Store pos in item
            setPos(stack, new Position(player.dimension, actualPos));
            if(world.isRemote)
                player.sendMessage(new TextComponentTranslation("message.tool.saved", actualPos.getX(), actualPos.getY(), actualPos.getZ()));
        }
        else if(position.dimensionId != player.dimension)
        {
            //Stored pos in different dimension! Remove stored pos
            setPos(stack, null);
            if(world.isRemote)
                player.sendMessage(new TextComponentTranslation("message.tool.diffdim"));
        }
        else
        {
            //Remove stored pos and finalise area
            //setPos(stack, null);
            player.openGui(LandManager.INSTANCE, 0, world, actualPos.getX(), actualPos.getY(), actualPos.getZ());
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        //Only work in main hand!
        if(hand == EnumHand.OFF_HAND) return super.onItemRightClick(world, player, hand);

        ItemStack stack = player.getHeldItem(hand);
        if(player.isSneaking() && getPos(stack) != null)
        {
            //Clear position
            setPos(stack, null);
            if(world.isRemote)
                player.sendMessage(new TextComponentTranslation("message.tool.cleared"));
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    private String posToString(BlockPos pos)
    {
        return String.format("%s, %s, %s", pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        Position pos = getPos(stack);
        String posText = pos == null ? I18n.format("item.admin.tooltip.notset") : I18n.format("item.admin.tooltip.set", pos.dimensionId, posToString(pos.position));
        tooltip.add(posText);
    }
}
