package org.classiclude.commons.network.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread factory used for creating threads for MMO server tasks.<br>
 * This factory assigns custom names and priorities to the threads it creates, aiding in identification and management.
 * @author JoeAlisson
 */
public class MMOThreadFactory implements ThreadFactory
{
	private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
	
	private final AtomicInteger _threadNumber = new AtomicInteger(1);
	private final String _namePrefix;
	private final int _priority;
	
	public MMOThreadFactory(String name, int priority)
	{
		_namePrefix = name + "-MMO-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
		_priority = priority;
	}
	
	@Override
	public Thread newThread(Runnable r)
	{
		final Thread thread = new Thread(null, r, _namePrefix + _threadNumber.getAndIncrement(), 0);
		thread.setPriority(_priority);
		thread.setDaemon(false);
		return thread;
	}
}
