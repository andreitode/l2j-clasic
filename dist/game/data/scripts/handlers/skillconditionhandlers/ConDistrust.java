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

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.instance.Chest;
import org.classiclude.gameserver.model.actor.instance.Monster;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.SystemMessageId;

/**
 * @author
 */
public class ConDistrust implements ISkillCondition
{
	public ConDistrust(StatSet params)
	{
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		final Player player = caster.asPlayer();
		final Creature effected = target.asCreature();
		
		if (player == null)
		{
			return false;
		}
		
		if (!(effected instanceof Monster) || (effected instanceof Chest) || effected.isRaid() || effected.isRaidMinion() || effected.isDead())
		{
			player.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		
		return true;
	}
}
