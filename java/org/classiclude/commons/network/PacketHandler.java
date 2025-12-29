package org.classiclude.commons.network;

/**
 * Responsible for handling incoming data and converting it into a readable packet.<br>
 * This functional interface defines a method to process raw data from a buffer and create a corresponding packet instance.
 * @param <T> The type of the client associated with the packet handler.
 * @author JoeAlisson
 */
@FunctionalInterface
public interface PacketHandler<T extends Client<Connection<T>>>
{
	/**
	 * Converts the data in the buffer into a readable packet.<br>
	 * This method should interpret the raw data and construct a packet instance that represents the data for further processing.
	 * @param buffer The buffer containing the data to be converted.
	 * @param client The client who sent the data.
	 * @return A {@link ReadablePacket} corresponding to the data received.
	 */
	ReadablePacket<T> handlePacket(ReadableBuffer buffer, T client);
}
