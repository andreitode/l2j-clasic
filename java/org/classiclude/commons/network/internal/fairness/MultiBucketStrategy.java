package org.classiclude.commons.network.internal.fairness;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.classiclude.commons.network.Client;

/**
 * Implements a fairness strategy using multiple buckets.<br>
 * This strategy distributes clients across a number of buckets and cycles through them to achieve fairness.
 * @author JoeAlisson
 */
public class MultiBucketStrategy implements FairnessStrategy
{
	private final Queue<Client<?>>[] _readyBuckets;
	private final int _fairnessBuckets;
	private int _nextOffer;
	private int _nextPoll;
	
	@SuppressWarnings("unchecked")
	MultiBucketStrategy(int fairnessBuckets)
	{
		_readyBuckets = new ConcurrentLinkedQueue[fairnessBuckets];
		_fairnessBuckets = fairnessBuckets;
		for (int i = 0; i < fairnessBuckets; i++)
		{
			_readyBuckets[i] = new ConcurrentLinkedQueue<>();
		}
	}
	
	@Override
	public void doNextAction(Client<?> client, Consumer<Client<?>> action)
	{
		final int offer = _nextOffer++ % _fairnessBuckets;
		Queue<Client<?>> nextBucket = _readyBuckets[offer];
		nextBucket.offer(client);
		
		final int poll = _nextPoll++ % _fairnessBuckets;
		
		nextBucket = _readyBuckets[poll];
		final Client<?> next = nextBucket.poll();
		if (next != null)
		{
			action.accept(next);
		}
	}
}
