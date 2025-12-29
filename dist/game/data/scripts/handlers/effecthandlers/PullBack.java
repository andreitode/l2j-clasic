/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.classiclude.gameserver.enums.FlyType;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.stats.Formulas;
import org.classiclude.gameserver.network.serverpackets.FlyToLocation;
import org.classiclude.gameserver.network.serverpackets.ValidateLocation;

/**
 * An effect that pulls effected target back to the effector.
 * @author Nik
 */
public class PullBack extends AbstractEffect
{
	private final int _speed;
	private final int _delay;
	private final int _animationSpeed;
	private final FlyType _type;
	
	public PullBack(StatSet params)
	{
		_speed = params.getInt("speed", 0);
		_delay = params.getInt("delay", _speed);
		_animationSpeed = params.getInt("animationSpeed", 0);
		_type = params.getEnum("type", FlyType.class, FlyType.WARP_FORWARD); // type 9
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return Formulas.calcProbability(100, effector, effected, skill);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		// Prevent pulling raids and town NPCs.
		if ((effected == null) || effected.isRaid() || (effected.isNpc() && !effected.isAttackable()))
		{
			return;
		}
		// Prevent pulling debuff blocked characters.
		if ((effected.isDebuffBlocked()))
		{
			return;
		}
		
		// In retail, you get debuff, but you are not even moved if there is obstacle. You are still disabled from using skills and moving though.
		if (GeoEngine.getInstance().canMoveToTarget(effected.getX(), effected.getY(), effected.getZ(), effector.getX(), effector.getY(), effector.getZ(), effected.getInstanceWorld()))
		{
			effected.broadcastPacket(new FlyToLocation(effected, effector, _type, _speed, _delay, _animationSpeed));
			effected.setXYZ(effector.getX(), effector.getY(), GeoEngine.getInstance().getHeight(effector.getX(), effector.getY(), effector.getZ() + 20));
			effected.broadcastPacket(new ValidateLocation(effected), false);
			effected.revalidateZone(true);
		}
	}
}
