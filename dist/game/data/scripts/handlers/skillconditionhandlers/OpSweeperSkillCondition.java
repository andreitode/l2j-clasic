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

import org.classiclude.Config;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Attackable;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.SystemMessageId;

/**
 * @author Zoey76
 */
public class OpSweeperSkillCondition implements ISkillCondition
{
	public OpSweeperSkillCondition(StatSet params)
	{
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		boolean canSweep = false;
		if (caster.isPlayer() && (skill != null))
		{
			final Player sweeper = caster.asPlayer();
			for (WorldObject wo : skill.getTargetsAffected(sweeper, target))
			{
				if ((wo != null) && wo.isAttackable())
				{
					final Attackable attackable = wo.asAttackable();
					if (attackable.isDead())
					{
						if (attackable.isSpoiled())
						{
							canSweep = attackable.checkSpoilOwner(sweeper, true);
							if (canSweep)
							{
								canSweep = !attackable.isOldCorpse(sweeper, Config.CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY, true);
							}
							if (canSweep)
							{
								canSweep = sweeper.getInventory().checkInventorySlotsAndWeight(attackable.getSpoilLootItems(), true, true);
							}
						}
						else
						{
							sweeper.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
						}
					}
				}
			}
		}
		return canSweep;
	}
}
