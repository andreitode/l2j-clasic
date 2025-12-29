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

import org.classiclude.gameserver.enums.FlyType;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.FlyToLocation;
import org.classiclude.gameserver.network.serverpackets.ValidateLocation;

/**
 * Throw Up effect implementation.
 */
public class FlyAway extends AbstractEffect
{
	private final int _radius;
	
	public FlyAway(StatSet params)
	{
		_radius = params.getInt("radius");
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final int dx = effector.getX() - effected.getX();
		final int dy = effector.getY() - effected.getY();
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		final double nRadius = effector.getCollisionRadius() + effected.getCollisionRadius() + _radius;
		
		final int x = (int) (effector.getX() - (nRadius * (dx / distance)));
		final int y = (int) (effector.getY() - (nRadius * (dy / distance)));
		final int z = effector.getZ();
		
		final Location destination = GeoEngine.getInstance().getValidLocation(effected.getX(), effected.getY(), effected.getZ(), x, y, z, effected.getInstanceWorld());
		
		effected.broadcastPacket(new FlyToLocation(effected, destination, FlyType.THROW_UP));
		effected.setXYZ(destination);
		effected.broadcastPacket(new ValidateLocation(effected));
		effected.revalidateZone(true);
	}
}
