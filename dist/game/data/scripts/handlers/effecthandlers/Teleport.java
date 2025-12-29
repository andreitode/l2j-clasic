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

import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.effects.EffectType;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * Teleport effect implementation.
 * @author Adry_85
 */
public class Teleport extends AbstractEffect
{
	private final Location _loc;
	
	public Teleport(StatSet params)
	{
		_loc = new Location(params.getInt("x", 0), params.getInt("y", 0), params.getInt("z", 0));
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.teleToLocation(_loc, true, null);
	}
}
