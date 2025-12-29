package org.classiclude.commons.network;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Defines a filter for incoming network connections.<br>
 * This functional interface is used to determine whether an incoming connection should be accepted or rejected.
 * @author JoeAlisson
 */
@FunctionalInterface
public interface ConnectionFilter
{
	/**
	 * Determines if a given connection channel should be accepted.<br>
	 * Implementations of this method should include logic to evaluate the acceptability of an incoming connection.
	 * @param channel The AsynchronousSocketChannel to be evaluated.
	 * @return true if the channel is acceptable, false otherwise.
	 */
	boolean accept(AsynchronousSocketChannel channel);
}
