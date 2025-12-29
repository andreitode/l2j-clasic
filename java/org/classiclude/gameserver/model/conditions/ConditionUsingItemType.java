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
package org.classiclude.gameserver.model.conditions;

import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.item.ItemTemplate;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.item.type.ArmorType;
import org.classiclude.gameserver.model.itemcontainer.Inventory;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * The Class ConditionUsingItemType.
 * @author mkizub, naker
 */
public class ConditionUsingItemType extends Condition
{
	private final boolean _armor;
	private final int _mask;
	
	/**
	 * Instantiates a new condition using item type.
	 * @param mask the mask
	 */
	public ConditionUsingItemType(int mask)
	{
		_mask = mask;
		_armor = (_mask & (ArmorType.MAGIC.mask() | ArmorType.LIGHT.mask() | ArmorType.HEAVY.mask())) != 0;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector == null)
		{
			return false;
		}
		
		if (!effector.isPlayer())
		{
			return !_armor && ((_mask & effector.getAttackType().mask()) != 0);
		}
		
		final Inventory inv = effector.getInventory();
		if (_armor)
		{
			
			final Item chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			final Item legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			
			if ((chest == null) && (legs == null))
			{
				return (ArmorType.NONE.mask() & _mask) == ArmorType.NONE.mask();
			}
			
			if ((chest != null) && (legs == null))
			{
				final int chestMask1 = chest.getTemplate().getItemMask();
				final int chestBodyPart = chest.getTemplate().getBodyPart();
				if (chestBodyPart == ItemTemplate.SLOT_FULL_ARMOR)
				{
					return (_mask & chestMask1) != 0;
				}
				
				return (ArmorType.NONE.mask() & _mask) == ArmorType.NONE.mask();
			}
			
			if ((chest != null) && (legs != null))
			{
				final int chestMask = chest.getTemplate().getItemMask();
				final int legMask = legs.getTemplate().getItemMask();
				
				if (chestMask == legMask)
				{
					return (_mask & chestMask) != 0;
				}
				
				return (ArmorType.NONE.mask() & _mask) == ArmorType.NONE.mask();
			}
		}
		
		return (_mask & inv.getWearedMask()) != 0;
	}
}
