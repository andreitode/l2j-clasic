/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package handlers.effecthandlers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.gameserver.ai.CtrlEvent;
import org.classiclude.gameserver.ai.CtrlIntention;
import org.classiclude.gameserver.enums.FlyType;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.effects.EffectFlag;
import org.classiclude.gameserver.model.effects.EffectType;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.stats.Formulas;
import org.classiclude.gameserver.network.serverpackets.FlyToLocation;
import org.classiclude.gameserver.network.serverpackets.ValidateLocation;
import org.classiclude.gameserver.util.Util;

/**
 * Check if this effect is not counted as being stunned.
 * @author UnAfraid
 */
public class KnockBack extends AbstractEffect
{
	private final int _distance;
	private final int _speed;
	private final int _delay;
	private final int _animationSpeed;
	private final boolean _knockDown;
	private final FlyType _type;
	
	private static final Set<Creature> ACTIVE_KNOCKBACKS = ConcurrentHashMap.newKeySet();
	
	public KnockBack(StatSet params)
	{
		_distance = params.getInt("distance", 50);
		_speed = params.getInt("speed", 0);
		_delay = params.getInt("delay", 0);
		_animationSpeed = params.getInt("animationSpeed", 0);
		_knockDown = params.getBoolean("knockDown", false);
		_type = params.getEnum("type", FlyType.class, _knockDown ? FlyType.PUSH_DOWN_HORIZONTAL : FlyType.PUSH_HORIZONTAL);
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return _knockDown || Formulas.calcProbability(100, effector, effected, skill);
	}
	
	@Override
	public boolean isInstant()
	{
		return !_knockDown;
	}
	
	@Override
	public long getEffectFlags()
	{
		return _knockDown ? EffectFlag.BLOCK_ACTIONS.getMask() : super.getEffectFlags();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return _knockDown ? EffectType.BLOCK_ACTIONS : super.getEffectType();
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!_knockDown)
		{
			knockBack(effector, effected);
		}
	}
	
	@Override
	public void continuousInstant(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.startParalyze();
		
		if (_knockDown)
		{
			knockBack(effector, effected);
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		ACTIVE_KNOCKBACKS.remove(effected);
		effected.updateAbnormalVisualEffects();
		
		if (!effected.isPlayer())
		{
			effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	private void knockBack(Creature effector, Creature effected)
	{
		if (!ACTIVE_KNOCKBACKS.contains(effected))
		{
			ACTIVE_KNOCKBACKS.add(effected);
			
			// Prevent knocking back raids and town NPCs.
			if (effected.isRaid() || (effected.isNpc() && !effected.isAttackable()))
			{
				return;
			}
			
			final double radians = Math.toRadians(Util.calculateAngleFrom(effector, effected));
			final int x = (int) (effected.getX() + (_distance * Math.cos(radians)));
			final int y = (int) (effected.getY() + (_distance * Math.sin(radians)));
			final int z = effected.getZ();
			final Location loc = GeoEngine.getInstance().getValidLocation(effected.getX(), effected.getY(), effected.getZ(), x, y, z, effected.getInstanceWorld());
			
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			effected.broadcastPacket(new FlyToLocation(effected, loc, _type, _speed, _delay, _animationSpeed));
			if (_knockDown)
			{
				effected.setHeading(Util.calculateHeadingFrom(effected, effector));
			}
			effected.setXYZ(loc);
			effected.broadcastPacket(new ValidateLocation(effected));
			effected.revalidateZone(true);
		}
	}
}
