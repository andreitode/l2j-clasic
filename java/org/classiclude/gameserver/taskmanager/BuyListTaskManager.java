package org.classiclude.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.buylist.Product;

/**
 * @author Mobius
 */
public class BuyListTaskManager
{
	protected static final Map<Product, Long> PRODUCTS = new ConcurrentHashMap<>();
	protected static final List<Product> PENDING_UPDATES = new ArrayList<>();
	protected static boolean _workingProducts = false;
	protected static boolean _workingSaves = false;
	
	protected BuyListTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new BuyListProductTask(), 1000, 60000);
		ThreadPool.scheduleAtFixedRate(new BuyListSaveTask(), 50, 50);
	}
	
	protected class BuyListProductTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingProducts)
			{
				return;
			}
			_workingProducts = true;
			
			final long currentTime = System.currentTimeMillis();
			for (Entry<Product, Long> entry : PRODUCTS.entrySet())
			{
				if (currentTime > entry.getValue().longValue())
				{
					final Product product = entry.getKey();
					PRODUCTS.remove(product);
					synchronized (PENDING_UPDATES)
					{
						if (!PENDING_UPDATES.contains(product))
						{
							PENDING_UPDATES.add(product);
						}
					}
				}
			}
			
			_workingProducts = false;
		}
	}
	
	protected class BuyListSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingSaves)
			{
				return;
			}
			_workingSaves = true;
			
			if (!PENDING_UPDATES.isEmpty())
			{
				final Product product;
				synchronized (PENDING_UPDATES)
				{
					product = PENDING_UPDATES.get(0);
					PENDING_UPDATES.remove(product);
				}
				product.restock();
			}
			
			_workingSaves = false;
		}
	}
	
	public void add(Product product, long endTime)
	{
		if (!PRODUCTS.containsKey(product))
		{
			PRODUCTS.put(product, endTime);
		}
	}
	
	public void update(Product product, long endTime)
	{
		PRODUCTS.put(product, endTime);
	}
	
	public long getRestockDelay(Product product)
	{
		return PRODUCTS.getOrDefault(product, 0L);
	}
	
	public static BuyListTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BuyListTaskManager INSTANCE = new BuyListTaskManager();
	}
}
