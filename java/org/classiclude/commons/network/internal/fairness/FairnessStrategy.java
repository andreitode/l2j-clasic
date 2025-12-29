package org.classiclude.commons.network.internal.fairness;

import java.util.function.Consumer;

import org.classiclude.commons.network.Client;

/**
 * Defines a strategy for managing fairness in executing actions across clients.<br>
 * Implementations of this interface will determine how actions are distributed fairly among clients.
 * @author JoeAlisson
 */
interface FairnessStrategy
{
	/**
	 * Executes an action for a client in a manner that ensures fairness across all clients.
	 * @param client The client for which the action is to be executed.
	 * @param action The action to be performed, represented as a Consumer of Client.
	 */
	void doNextAction(Client<?> client, Consumer<Client<?>> action);
}
