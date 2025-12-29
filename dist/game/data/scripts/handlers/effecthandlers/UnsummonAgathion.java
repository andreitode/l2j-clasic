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
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerUnsummonAgathion;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.ExUserInfoCubic;

/**
 * Unsummon Agathion effect implementation.
 * @author Zoey76
 */
public class UnsummonAgathion extends AbstractEffect
{
	public UnsummonAgathion(StatSet params)
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
		final Player player = effector.asPlayer();
		if (player != null)
		{
			final int agathionId = player.getAgathionId();
			if (agathionId > 0)
			{
				player.setAgathionId(0);
				player.sendPacket(new ExUserInfoCubic(player));
				player.broadcastCharInfo();
				
				if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_UNSUMMON_AGATHION))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerUnsummonAgathion(player, agathionId));
				}
			}
		}
	}
}
