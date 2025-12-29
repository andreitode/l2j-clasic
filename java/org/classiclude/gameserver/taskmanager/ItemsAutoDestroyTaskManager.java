package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.Config;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.enums.ItemLocation;
import org.classiclude.gameserver.instancemanager.ItemsOnGroundManager;
import org.classiclude.gameserver.model.item.instance.Item;

public class ItemsAutoDestroyTaskManager implements Runnable
{
	private static final Set<Item> ITEMS = ConcurrentHashMap.newKeySet();
	
	protected ItemsAutoDestroyTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 5000, 5000);
	}
	
	@Override
	public void run()
	{
		if (ITEMS.isEmpty())
		{
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		final Iterator<Item> iterator = ITEMS.iterator();
		Item itemInstance;
		
		while (iterator.hasNext())
		{
			itemInstance = iterator.next();
			if ((itemInstance.getDropTime() == 0) || (itemInstance.getItemLocation() != ItemLocation.VOID))
			{
				iterator.remove();
			}
			else
			{
				final long autoDestroyTime;
				if (itemInstance.getTemplate().getAutoDestroyTime() > 0)
				{
					autoDestroyTime = itemInstance.getTemplate().getAutoDestroyTime();
				}
				else if (itemInstance.getTemplate().hasExImmediateEffect())
				{
					autoDestroyTime = Config.HERB_AUTO_DESTROY_TIME;
				}
				else
				{
					autoDestroyTime = ((Config.AUTODESTROY_ITEM_AFTER == 0) ? 3600000 : Config.AUTODESTROY_ITEM_AFTER * 1000);
				}
				
				if ((currentTime - itemInstance.getDropTime()) > autoDestroyTime)
				{
					itemInstance.decayMe();
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(itemInstance);
					}
					iterator.remove();
				}
			}
		}
	}
	
	public void addItem(Item item)
	{
		item.setDropTime(System.currentTimeMillis());
		ITEMS.add(item);
	}
	
	public static ItemsAutoDestroyTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroyTaskManager INSTANCE = new ItemsAutoDestroyTaskManager();
	}
}