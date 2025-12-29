package org.classiclude.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.actor.Creature;

/**
 * @author Mobius
 */
public class CreatureSeeTaskManager implements Runnable
{
	private static final Set<Creature> CREATURES = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	
	protected CreatureSeeTaskManager()
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
		
		for (Creature creature : CREATURES)
		{
			creature.updateSeenCreatures();
		}
		
		_working = false;
	}
	
	public void add(Creature creature)
	{
		CREATURES.add(creature);
	}
	
	public void remove(Creature creature)
	{
		CREATURES.remove(creature);
	}
	
	public static CreatureSeeTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CreatureSeeTaskManager INSTANCE = new CreatureSeeTaskManager();
	}
}
