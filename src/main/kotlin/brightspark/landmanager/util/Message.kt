package brightspark.landmanager.util

import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.simple.SimpleChannel

/**
 * Interface for a network message that can be registered with the custom overload of [SimpleChannel.registerMessage]
 * which takes a reified parameter of a child of [Message]
 *
 * Make sure classes implementing this interface always have a default constructor with no arguments
 */
interface Message {
	/**
	 * Reads the [Message] from the [buffer]
	 */
	fun encode(buffer: PacketBuffer)

	/**
	 * Writes the [Message] to the [buffer]
	 */
	fun decode(buffer: PacketBuffer)

	/**
	 * Handles this [Message]
	 */
	fun consume(context: NetworkEvent.Context)
}
