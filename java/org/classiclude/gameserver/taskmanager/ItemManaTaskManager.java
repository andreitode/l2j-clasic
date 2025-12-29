package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.item.instance.Item;

/**
 * @author Mobius
 */
public class ItemManaTaskManager implements Runnable
{
	private static final Map<Item, Long> ITEMS = new ConcurrentHashMap<>();
	private static final int MANA_CONSUMPTION_RATE = 60000;
	private static boolean _working = false;
	
	protected ItemManaTaskManager()
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
					iterator.remove();
					
					final Item item = entry.getKey();
					final Player player = item.asPlayer();
					if ((player == null) || player.isInOfflineMode())
					{
						continue;
					}
					
					item.decreaseMana(item.isEquipped());
				}
			}
		}
		
		_working = false;
	}
	
	public void add(Item item)
	{
		if (!ITEMS.containsKey(item))
		{
			ITEMS.put(item, System.currentTimeMillis() + MANA_CONSUMPTION_RATE);
		}
	}
	
	public static ItemManaTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemManaTaskManager INSTANCE = new ItemManaTaskManager();
	}
}