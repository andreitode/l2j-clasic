package org.classiclude.gameserver.taskmanager;

import static org.classiclude.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.ai.CreatureAI;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;

/**
 * @author Mobius
 */
public class CreatureFollowTaskManager
{
	protected static final Map<Creature, Integer> NORMAL_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	protected static final Map<Creature, Integer> ATTACK_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	protected static boolean _workingNormal = false;
	protected static boolean _workingAttack = false;
	
	protected CreatureFollowTaskManager()
	{
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureFollowNormalTask(), 1000, 1000);
		ThreadPool.schedulePriorityTaskAtFixedRate(new CreatureFollowAttackTask(), 500, 500);
	}
	
	protected class CreatureFollowNormalTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingNormal)
			{
				return;
			}
			_workingNormal = true;
			
			if (!NORMAL_FOLLOW_CREATURES.isEmpty())
			{
				for (Entry<Creature, Integer> entry : NORMAL_FOLLOW_CREATURES.entrySet())
				{
					follow(entry.getKey(), entry.getValue());
				}
			}
			
			_workingNormal = false;
		}
	}
	
	protected class CreatureFollowAttackTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingAttack)
			{
				return;
			}
			_workingAttack = true;
			
			if (!ATTACK_FOLLOW_CREATURES.isEmpty())
			{
				for (Entry<Creature, Integer> entry : ATTACK_FOLLOW_CREATURES.entrySet())
				{
					follow(entry.getKey(), entry.getValue());
				}
			}
			
			_workingAttack = false;
		}
	}
	
	protected void follow(Creature creature, int range)
	{
		try
		{
			if (creature.hasAI())
			{
				final CreatureAI ai = creature.getAI();
				if (ai != null)
				{
					final WorldObject followTarget = ai.getTarget();
					if (followTarget == null)
					{
						if (creature.isSummon())
						{
							creature.asSummon().setFollowStatus(false);
						}
						ai.setIntention(AI_INTENTION_IDLE);
						return;
					}
					
					final int followRange = range == -1 ? Rnd.get(50, 100) : range;
					if (!creature.isInsideRadius3D(followTarget, followRange))
					{
						if (!creature.isInsideRadius3D(followTarget, 3000))
						{
							// If the target is too far (maybe also teleported).
							if (creature.isSummon())
							{
								creature.asSummon().setFollowStatus(false);
							}
							ai.setIntention(AI_INTENTION_IDLE);
							return;
						}
						ai.moveToPawn(followTarget, followRange);
					}
				}
				else
				{
					remove(creature);
				}
			}
			else
			{
				remove(creature);
			}
		}
		catch (Exception e)
		{
			// Ignore.
		}
	}
	
	public boolean isFollowing(Creature creature)
	{
		return NORMAL_FOLLOW_CREATURES.containsKey(creature) || ATTACK_FOLLOW_CREATURES.containsKey(creature);
	}
	
	public void addNormalFollow(Creature creature, int range)
	{
		NORMAL_FOLLOW_CREATURES.putIfAbsent(creature, range);
		follow(creature, range);
	}
	
	public void addAttackFollow(Creature creature, int range)
	{
		ATTACK_FOLLOW_CREATURES.putIfAbsent(creature, range);
		follow(creature, range);
	}
	
	public void remove(Creature creature)
	{
		NORMAL_FOLLOW_CREATURES.remove(creature);
		ATTACK_FOLLOW_CREATURES.remove(creature);
	}
	
	public static CreatureFollowTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CreatureFollowTaskManager INSTANCE = new CreatureFollowTaskManager();
	}
}
