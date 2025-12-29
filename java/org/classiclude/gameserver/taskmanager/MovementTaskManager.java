package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.commons.util.CommonUtil;
import org.classiclude.gameserver.ai.CtrlEvent;
import org.classiclude.gameserver.model.actor.Creature;

/**
 * Movement task manager class.
 * @author Mobius
 */
public class MovementTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(MovementTaskManager.class.getName());
	
	private static final Set<Set<Creature>> POOLS_CREATURE = ConcurrentHashMap.newKeySet();
	private static final Set<Set<Creature>> POOLS_PLAYER = ConcurrentHashMap.newKeySet();
	private static final int POOL_SIZE_CREATURE = 1000;
	private static final int POOL_SIZE_PLAYER = 500;
	private static final int TASK_DELAY_CREATURE = 100;
	private static final int TASK_DELAY_PLAYER = 50;
	
	protected MovementTaskManager()
	{
	}
	
	private class Movement implements Runnable
	{
		private final Set<Creature> _creatures;
		
		public Movement(Set<Creature> creatures)
		{
			_creatures = creatures;
		}
		
		@Override
		public void run()
		{
			if (_creatures.isEmpty())
			{
				return;
			}
			
			Creature creature;
			final Iterator<Creature> iterator = _creatures.iterator();
			while (iterator.hasNext())
			{
				creature = iterator.next();
				try
				{
					if (creature.updatePosition())
					{
						iterator.remove();
						creature.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
					}
				}
				catch (Exception e)
				{
					iterator.remove();
					LOGGER.warning("MovementTaskManager: Problem updating position of " + creature);
					LOGGER.warning(CommonUtil.getStackTrace(e));
				}
			}
		}
	}
	
	/**
	 * Add a Creature to moving objects of MovementTaskManager.
	 * @param creature The Creature to add to moving objects of MovementTaskManager.
	 */
	public synchronized void registerMovingObject(Creature creature)
	{
		if (creature.isPlayer())
		{
			for (Set<Creature> pool : POOLS_PLAYER)
			{
				if (pool.contains(creature))
				{
					return;
				}
			}
			for (Set<Creature> pool : POOLS_PLAYER)
			{
				if (pool.size() < POOL_SIZE_PLAYER)
				{
					pool.add(creature);
					return;
				}
			}
			
			final Set<Creature> pool = ConcurrentHashMap.newKeySet(POOL_SIZE_PLAYER);
			pool.add(creature);
			ThreadPool.schedulePriorityTaskAtFixedRate(new Movement(pool), TASK_DELAY_PLAYER, TASK_DELAY_PLAYER);
			POOLS_PLAYER.add(pool);
		}
		
		else
		{
			for (Set<Creature> pool : POOLS_CREATURE)
			{
				if (pool.contains(creature))
				{
					return;
				}
			}
			for (Set<Creature> pool : POOLS_CREATURE)
			{
				if (pool.size() < POOL_SIZE_CREATURE)
				{
					pool.add(creature);
					return;
				}
			}
			
			final Set<Creature> pool = ConcurrentHashMap.newKeySet(POOL_SIZE_CREATURE);
			pool.add(creature);
			ThreadPool.scheduleAtFixedRate(new Movement(pool), TASK_DELAY_CREATURE, TASK_DELAY_CREATURE);
			POOLS_CREATURE.add(pool);
		}
		
	}
	
	public static final MovementTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MovementTaskManager INSTANCE = new MovementTaskManager();
	}
}
