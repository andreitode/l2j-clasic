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

import org.classiclude.gameserver.ai.CtrlIntention;
import org.classiclude.gameserver.enums.FlyType;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.effects.EffectType;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.FlyToLocation;
import org.classiclude.gameserver.network.serverpackets.ValidateLocation;
import org.classiclude.gameserver.util.Util;

/**
 * Teleport To Target effect implementation.
 * @author Didldak, Adry_85
 */
public class TeleportToSummon extends AbstractEffect
{
	private final double _maxDistance;
	
	public TeleportToSummon(StatSet params)
	{
		_maxDistance = params.getDouble("distance", -1);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT_TO_TARGET;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return effected.hasServitors();
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Summon summon = effected.asPlayer().getFirstServitor();
		
		if ((_maxDistance > 0) && (effector.calculateDistance2D(summon) >= _maxDistance))
		{
			return;
		}
		
		final int px = summon.getX();
		final int py = summon.getY();
		double ph = Util.convertHeadingToDegree(summon.getHeading());
		
		ph += 180;
		if (ph > 360)
		{
			ph -= 360;
		}
		
		ph = (Math.PI * ph) / 180;
		final int x = (int) (px + (25 * Math.cos(ph)));
		final int y = (int) (py + (25 * Math.sin(ph)));
		final int z = summon.getZ();
		
		final Location loc = GeoEngine.getInstance().getValidLocation(effector.getX(), effector.getY(), effector.getZ(), x, y, z, effector.getInstanceWorld());
		
		effector.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		effector.broadcastPacket(new FlyToLocation(effector, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
		effector.abortAttack();
		effector.abortCast();
		effector.setXYZ(loc);
		effector.broadcastPacket(new ValidateLocation(effector));
		effected.revalidateZone(true);
	}
}
