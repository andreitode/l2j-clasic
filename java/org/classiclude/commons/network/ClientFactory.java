package org.classiclude.commons.network;

/**
 * Defines a factory interface for creating Client instances.<br>
 * This functional interface specifies a method to create a new Client using a given connection.
 * @param <T> The type of Client to be created, extending Client with a specified Connection type.
 * @author JoeAlisson
 */
@FunctionalInterface
public interface ClientFactory<T extends Client<Connection<T>>>
{
	/**
	 * Creates a new Client instance using the provided connection.<br>
	 * Implementations of this method should provide the logic to instantiate a specific Client type.
	 * @param connection The underlying connection to the client.
	 * @return A new instance of a client implementation.
	 */
	T create(Connection<T> connection);
}
