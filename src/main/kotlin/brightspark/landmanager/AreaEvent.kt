package brightspark.landmanager

import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.data.requests.Request
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.eventbus.api.Event

/**
 * Base event class for all Land Manager events
 */
sealed class AreaEvent(val area: Area) : Event()

/**
 * Fired server side when an area is deleted
 */
class AreaDeletedEvent(area: Area) : AreaEvent(area)

/**
 * Fired server side when an area is about to be created.
 * <p>
 * Cancel to prevent the area from being created. Cancelling this event will not notify the user of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
class AreaCreationEvent(area: Area) : AreaEvent(area) {
	override fun isCancelable(): Boolean = true
}

/**
 * Fired server side when a player claims an area directly with the claim command.
 * <p>
 * Cancel to prevent the player from claiming the area. Cancelling this event will not notify the user of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
class AreaClaimEvent(val player: PlayerEntity, area: Area) : AreaEvent(area) {
	override fun isCancelable(): Boolean = true
}

/**
 * Fired server side when a claim request is about to be approved.
 * <p>
 * Cancel to prevent the claim request from being approved. Cancelling this event will not notify the player who
 * requested the claim or the command sender of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
class AreaClaimApprovalEvent(val request: Request, area: Area, val sender: CommandSource) : AreaEvent(area) {
	override fun isCancelable(): Boolean = true
}

/**
 * Fired server side when a claim request is about to be disapproved.
 * <p>
 * Cancel to prevent the claim request from being disapproved. Cancelling this event will not notify the player who
 * requested the claim or the command sender of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
class AreaClaimDisapprovalEvent(val request: Request, area: Area, val sender: CommandSource) : AreaEvent(area) {
	override fun isCancelable(): Boolean = true
}
