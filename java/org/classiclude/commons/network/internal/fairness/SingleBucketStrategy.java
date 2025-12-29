package org.classiclude.commons.network.internal.fairness;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.classiclude.commons.network.Client;

/**
 * Implements a single bucket strategy for fairness.<br>
 * This strategy uses a single queue to manage clients and ensures fair processing of actions by cycling through them in order.
 * @author JoeAlisson
 */
public class SingleBucketStrategy implements FairnessStrategy
{
	private final ConcurrentLinkedQueue<Client<?>> _readyClients = new ConcurrentLinkedQueue<>();
	
	@Override
	public void doNextAction(Client<?> client, Consumer<Client<?>> action)
	{
		_readyClients.offer(client);
		final Client<?> next = _readyClients.poll();
		if (next != null)
		{
			action.accept(next);
		}
	}
}
