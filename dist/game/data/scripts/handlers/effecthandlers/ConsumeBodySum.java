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

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.EffectScope;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.stats.Formulas;
import org.classiclude.gameserver.taskmanager.DecayTaskManager;

/**
 * Consume Body effect implementation.
 * @author Mobius
 */
public class ConsumeBodySum extends AbstractEffect
{
	public ConsumeBodySum(StatSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((effector == null) || (effected == null) || !effected.isNpc() || !effected.isDead())
		{
			return;
		}
		
		if (skill.hasEffects(EffectScope.START))
		{
			DecayTaskManager.getInstance().cancel(effected);
			ThreadPool.schedule(() -> effected.onDecay(), Formulas.calcAtkSpd(effector, skill, skill.getHitTime() + skill.getCoolTime()));
		}
		else
		{
			effected.asNpc().endDecayTask();
		}
	}
}
