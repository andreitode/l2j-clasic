package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.Spawn;
import org.classiclude.gameserver.model.actor.Npc;

/**
 * @author Mobius
 */
public class RespawnTaskManager implements Runnable
{
	private static final Map<Npc, Long> PENDING_RESPAWNS = new ConcurrentHashMap<>();
	private static boolean _working = false;
	
	protected RespawnTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 0, 1000);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		if (!PENDING_RESPAWNS.isEmpty())
		{
			final long currentTime = System.currentTimeMillis();
			final Iterator<Entry<Npc, Long>> iterator = PENDING_RESPAWNS.entrySet().iterator();
			Entry<Npc, Long> entry;
			
			while (iterator.hasNext())
			{
				entry = iterator.next();
				if (currentTime > entry.getValue())
				{
					iterator.remove();
					
					final Npc npc = entry.getKey();
					final Spawn spawn = npc.getSpawn();
					if (spawn != null)
					{
						spawn.respawnNpc(npc);
						spawn._scheduledCount--;
					}
				}
			}
		}
		
		_working = false;
	}
	
	public void add(Npc npc, long time)
	{
		PENDING_RESPAWNS.put(npc, time);
	}
	
	public static RespawnTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RespawnTaskManager INSTANCE = new RespawnTaskManager();
	}
}
