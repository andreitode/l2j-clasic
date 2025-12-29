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

import java.util.HashSet;
import java.util.Set;

import org.classiclude.gameserver.data.xml.ClanHallData;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.residences.ClanHall;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * @author Sdw
 */
public class OpCheckResidenceSkillCondition implements ISkillCondition
{
	private final Set<Integer> _residenceIds = new HashSet<>();
	private final boolean _isWithin;
	
	public OpCheckResidenceSkillCondition(StatSet params)
	{
		_residenceIds.addAll(params.getList("residenceIds", Integer.class));
		_isWithin = params.getBoolean("isWithin");
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (caster.isPlayer())
		{
			final Clan clan = caster.asPlayer().getClan();
			if (clan != null)
			{
				final ClanHall clanHall = ClanHallData.getInstance().getClanHallByClan(clan);
				if (clanHall != null)
				{
					return _isWithin ? _residenceIds.contains(clanHall.getResidenceId()) : !_residenceIds.contains(clanHall.getResidenceId());
				}
			}
		}
		return false;
	}
}
