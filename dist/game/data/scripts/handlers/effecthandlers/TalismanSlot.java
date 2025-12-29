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

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * Talisman Slot effect implementation.
 * @author Adry_85
 */
public class TalismanSlot extends AbstractEffect
{
	private final int _slots;
	
	public TalismanSlot(StatSet params)
	{
		_slots = params.getInt("slots", 0);
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return (effector != null) && (effected != null) && effected.isPlayer();
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.asPlayer().getStat().addTalismanSlots(_slots);
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.asPlayer().getStat().addTalismanSlots(-_slots);
	}
}
