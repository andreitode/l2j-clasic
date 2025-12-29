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
package handlers.skillconditionhandlers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.item.Weapon;
import org.classiclude.gameserver.model.item.type.WeaponType;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * @author UnAfraid
 */
public class OpTargetWeaponAttackTypeSkillCondition implements ISkillCondition
{
	private final Set<WeaponType> _weaponTypes = EnumSet.noneOf(WeaponType.class);
	
	public OpTargetWeaponAttackTypeSkillCondition(StatSet params)
	{
		final List<String> weaponTypes = params.getList("weaponType", String.class);
		if (weaponTypes != null)
		{
			for (String type : weaponTypes)
			{
				_weaponTypes.add(WeaponType.valueOf(type));
			}
		}
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if ((target == null) || !target.isCreature())
		{
			return false;
		}
		
		final Creature targetCreature = target.asCreature();
		final Weapon weapon = targetCreature.getActiveWeaponItem();
		if (weapon == null)
		{
			return false;
		}
		
		final WeaponType equippedType = weapon.getItemType();
		for (WeaponType weaponType : _weaponTypes)
		{
			if (weaponType == equippedType)
			{
				return true;
			}
		}
		
		return false;
	}
}
