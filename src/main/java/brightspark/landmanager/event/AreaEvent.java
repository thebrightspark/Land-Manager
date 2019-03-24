package brightspark.landmanager.event;

import brightspark.landmanager.data.areas.Area;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base event class for all Land Manager events
 */
public class AreaEvent extends Event
{
	protected Area area;

	public AreaEvent(Area area)
	{
		this.area = area;
	}

	/**
	 * @return The area this event relates to
	 */
	public Area getArea()
	{
		return area;
	}
}
