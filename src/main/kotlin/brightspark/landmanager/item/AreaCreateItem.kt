package brightspark.landmanager.item

import brightspark.ksparklib.api.sendToPlayer
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Position
import brightspark.landmanager.message.MessageOpenCreateAreaGui
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class AreaCreateItem : Item(Properties().apply {
	group(LandManager.group)
	maxStackSize(1)
}) {
	override fun hasEffect(stack: ItemStack): Boolean = true

	override fun onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType {
		// Only work in main hand!
		if (context.hand != Hand.MAIN_HAND)
			return super.onItemUseFirst(stack, context)

		val player = context.player!!
		val pos1 = getPos(stack)
		val pos2 = if (player.isSneaking) context.pos else context.pos.offset(context.face)
		when {
			pos1 == null -> {
				// Store pos in item
				setPos(stack, Position(player.dimension.id, pos2))
				player.sendMessage(TranslationTextComponent("message.landmanager.tool.saved", pos2.x, pos2.y, pos2.z))
			}
			pos1.dimensionId != player.dimension.id -> {
				//  Stored pos in different dimension! Remove stored pos
				setPos(stack, null)
				player.sendMessage(TranslationTextComponent("message.landmanager.tool.diffdim"))
			}
			else -> if (!context.world.isRemote)
				LandManager.NETWORK.sendToPlayer(MessageOpenCreateAreaGui(pos1.position, pos2), player as ServerPlayerEntity)
		}

		return ActionResultType.SUCCESS
	}

	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		// Only work in main hand!
		if (hand != Hand.MAIN_HAND)
			return super.onItemRightClick(world, player, hand)

		val stack = player.getHeldItem(hand)
		if (player.isSneaking && getPos(stack) != null) {
			// Clear position
			setPos(stack, null)
			if (world.isRemote)
				player.sendMessage(TranslationTextComponent("message.landmanager.tool.cleared"))
			return ActionResult(ActionResultType.SUCCESS, stack)
		}

		return super.onItemRightClick(world, player, hand)
	}

	override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<ITextComponent>, flag: ITooltipFlag) {
		tooltip.add(getPos(stack)?.let { TranslationTextComponent("item.landmanager.area_create.tooltip.set", it.dimensionId, posToString(it.position)) }
			?: TranslationTextComponent("item.landmanager.area_create.tooltip.notset"))
	}

	private fun posToString(pos: BlockPos) = "${pos.x}, ${pos.y}, ${pos.z}"

	companion object {
		fun setPos(stack: ItemStack, position: Position?) {
			if (position == null)
				stack.tag = null
			else
				stack.setTagInfo("pos", position.serializeNBT())
		}

		fun getPos(stack: ItemStack): Position? = stack.tag?.let { Position(it.getCompound("pos")) }
	}
}
