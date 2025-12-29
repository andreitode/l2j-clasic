package org.classiclude.commons.network;

/**
 * Defines a functional interface for executing incoming network packets.<br>
 * Implementations should handle the processing of packets, ideally offloading long-running or blocking operations to separate threads.
 * @param <T> The type of Client associated with the packet to be executed.
 * @author JoeAlisson
 */
@FunctionalInterface
public interface PacketExecutor<T extends Client<Connection<T>>>
{
	/**
	 * Executes the provided packet.<br>
	 * It is highly recommended to execute long-running or blocking code in another thread to avoid network processing delays.
	 * @param packet The packet to be executed.
	 */
	void execute(ReadablePacket<T> packet);
}
