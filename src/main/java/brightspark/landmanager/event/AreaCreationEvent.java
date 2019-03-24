package brightspark.landmanager.event;

import brightspark.landmanager.data.areas.Area;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Fired server side when an area is about to be created.
 *
 * Cancel to prevent the area from being created. Cancelling this event will not notify the user of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
@Cancelable
public class AreaCreationEvent extends AreaEvent
{
	public AreaCreationEvent(Area area)
	{
		super(area);
	}
}
