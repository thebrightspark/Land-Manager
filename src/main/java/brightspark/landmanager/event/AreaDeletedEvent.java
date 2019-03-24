package brightspark.landmanager.event;

import brightspark.landmanager.data.areas.Area;

/**
 * Fired server side when an area is deleted
 */
public class AreaDeletedEvent extends AreaEvent
{
	public AreaDeletedEvent(Area area)
	{
		super(area);
	}
}
