package brightspark.landmanager.event;

import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.requests.Request;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Fired server side when a claim request is about to be approved.
 * <p>
 * Cancel to prevent the claim request from being approved. Cancelling this event will not notify the player who
 * requested the claim or the command sender of what happened.
 * Handlers that cancel this should have their own way of notifying the user if necessary.
 */
@Cancelable
public class AreaClaimApprovalEvent extends AreaEvent {
	private Request request;
	private ICommandSender commandSender;

	public AreaClaimApprovalEvent(Area area, Request request, ICommandSender commandSender) {
		super(area);
		this.request = request;
		this.commandSender = commandSender;
	}

	/**
	 * @return The request that is about to be approved
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return The command sender who is accepting the claim request
	 */
	public ICommandSender getCommandSender() {
		return commandSender;
	}
}
