package org.classiclude.gameserver.ai;

import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.actor.Attackable;
import org.classiclude.gameserver.model.actor.Creature;

public class DistrustAI extends AttackableAI
{
	private final Creature _forcedTarget;
	
	public DistrustAI(Attackable actor, Creature forcedTarget)
	{
		super(actor);
		_forcedTarget = forcedTarget;
	}
	
	@Override
	public void thinkAttack()
	{
		if ((_forcedTarget == null) || _forcedTarget.isDead())
		{
			_actor.setTarget(null);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		
		_actor.setTarget(_forcedTarget);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, _forcedTarget);
		
		int range = _actor.getPhysicalAttackRange() + _forcedTarget.getTemplate().getCollisionRadius();
		
		if (_actor.calculateDistance2D(_forcedTarget) > range)
		{
			moveToPawn(_forcedTarget, range);
			return;
		}
		
		if (!GeoEngine.getInstance().canSeeTarget(_actor, _forcedTarget))
		{
			return;
		}
		
		_actor.doAutoAttack(_forcedTarget);
	}
}
