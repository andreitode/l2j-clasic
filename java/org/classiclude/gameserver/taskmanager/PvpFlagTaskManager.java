package org.classiclude.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.actor.Player;

/**
 * @author Mobius
 */
public class PvpFlagTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	
	protected PvpFlagTaskManager()
	{
		ThreadPool.schedulePriorityTaskAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		if (!PLAYERS.isEmpty())
		{
			final long currentTime = System.currentTimeMillis();
			for (Player player : PLAYERS)
			{
				if (currentTime > player.getPvpFlagLasts())
				{
					player.stopPvPFlag();
				}
				else if (currentTime > (player.getPvpFlagLasts() - 20000))
				{
					player.updatePvPFlag(2);
				}
				else
				{
					player.updatePvPFlag(1);
				}
			}
		}
		
		_working = false;
	}
	
	public void add(Player player)
	{
		PLAYERS.add(player);
	}
	
	public void remove(Player player)
	{
		PLAYERS.remove(player);
	}
	
	public static PvpFlagTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PvpFlagTaskManager INSTANCE = new PvpFlagTaskManager();
	}
}
