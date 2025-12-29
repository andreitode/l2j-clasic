package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.Config;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.actor.Player;

/**
 * @author Mobius
 */
public class PlayerAutoSaveTaskManager implements Runnable
{
	private static final Map<Player, Long> PLAYER_TIMES = new ConcurrentHashMap<>();
	private static boolean _working = false;
	
	protected PlayerAutoSaveTaskManager()
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
		
		if (!PLAYER_TIMES.isEmpty())
		{
			final long currentTime = System.currentTimeMillis();
			final Iterator<Entry<Player, Long>> iterator = PLAYER_TIMES.entrySet().iterator();
			Entry<Player, Long> entry;
			Player player;
			Long time;
			
			while (iterator.hasNext())
			{
				entry = iterator.next();
				player = entry.getKey();
				time = entry.getValue();
				
				if (currentTime > time)
				{
					if ((player != null) && player.isOnline())
					{
						player.autoSave();
						PLAYER_TIMES.put(player, currentTime + Config.CHAR_DATA_STORE_INTERVAL);
						break; // Prevent SQL flood.
					}
					
					iterator.remove();
				}
			}
		}
		
		_working = false;
	}
	
	public void add(Player player)
	{
		PLAYER_TIMES.put(player, System.currentTimeMillis() + Config.CHAR_DATA_STORE_INTERVAL);
	}
	
	public void remove(Player player)
	{
		PLAYER_TIMES.remove(player);
	}
	
	public static PlayerAutoSaveTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerAutoSaveTaskManager INSTANCE = new PlayerAutoSaveTaskManager();
	}
}
