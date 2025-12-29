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

import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Gabriel Costa Souza
 */
public class VipUp extends AbstractEffect
{
	private final long _amount;
	
	public VipUp(StatSet params)
	{
		_amount = params.getLong("amount", 0L);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected == null)
		{
			return;
		}
		
		final Player player = effected.asPlayer();
		if (player == null)
		{
			return;
		}
		
		player.updateVipPoints(_amount);
		player.sendPacket(new CreatureSay(player, ChatType.WHISPER, "VIP System", "Your VIP points have been increased giving you 30 days of VIP."));
	}
}
