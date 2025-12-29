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

import java.util.List;

import org.classiclude.gameserver.enums.DispelSlotType;
import org.classiclude.gameserver.enums.SkillFinishType;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.effects.EffectType;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.BuffInfo;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.stats.Formulas;

/**
 * Dispel By Category effect implementation.
 * @author DS, Adry_85
 */
public class DispelByCategory extends AbstractEffect
{
	private final DispelSlotType _slot;
	private final int _rate;
	private final int _max;
	
	public DispelByCategory(StatSet params)
	{
		_slot = params.getEnum("slot", DispelSlotType.class, DispelSlotType.BUFF);
		_rate = params.getInt("rate", 0);
		_max = params.getInt("max", 0);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.DISPEL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((skill == null) || effected.isDead() || effected.isRaid())
		{
			return;
		}
		
		final List<BuffInfo> canceled = Formulas.calcCancelStealEffects(effector, effected, skill, _slot, _rate, _max);
		for (BuffInfo can : canceled)
		{
			effected.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, can.getSkill());
		}
	}
}