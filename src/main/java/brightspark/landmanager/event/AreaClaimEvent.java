package brightspark.landmanager.event;

import brightspark.landmanager.data.areas.Area;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Fired server side when a player claims an area directly with the claim command.
 * <p>
 * Cancel to prevent the player from claiming the area. Cancelling this event will not notify the user of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
@Cancelable
public class AreaClaimEvent extends AreaEvent {
	private EntityPlayer player;

	public AreaClaimEvent(Area area, EntityPlayer player) {
		super(area);
		this.player = player;
	}

	/**
	 * @return The player who claimed the area
	 */
	public EntityPlayer getPlayer() {
		return player;
	}
}
