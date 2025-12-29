package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.item.instance.Item;

/**
 * @author Mobius
 */
public class ItemLifeTimeTaskManager implements Runnable
{
	private static final Map<Item, Long> ITEMS = new ConcurrentHashMap<>();
	private static boolean _working = false;
	
	protected ItemLifeTimeTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		if (!ITEMS.isEmpty())
		{
			final long currentTime = System.currentTimeMillis();
			final Iterator<Entry<Item, Long>> iterator = ITEMS.entrySet().iterator();
			Entry<Item, Long> entry;
			
			while (iterator.hasNext())
			{
				entry = iterator.next();
				if (currentTime > entry.getValue())
				{
					entry.getKey().endOfLife();
					iterator.remove();
				}
			}
		}
		
		_working = false;
	}
	
	public void add(Item item, long endTime)
	{
		if (!ITEMS.containsKey(item))
		{
			ITEMS.put(item, endTime);
		}
	}
	
	public void remove(Item item)
	{
		ITEMS.remove(item);
	}
	
	public static ItemLifeTimeTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemLifeTimeTaskManager INSTANCE = new ItemLifeTimeTaskManager();
	}
}
