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
package handlers.playeractions;

import org.classiclude.gameserver.handler.IPlayerActionHandler;
import org.classiclude.gameserver.model.ActionDataHolder;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.network.SystemMessageId;

/**
 * Unsummon Servitor player action handler.
 * @author St3eT
 */
public class UnsummonServitor implements IPlayerActionHandler
{
	@Override
	public void useAction(Player player, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		boolean canUnsummon = true;
		if (player.hasServitors())
		{
			for (Summon s : player.getServitors().values())
			{
				if (s.isBetrayed())
				{
					player.sendPacket(SystemMessageId.YOUR_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
					canUnsummon = false;
					break;
				}
				else if (s.isAttackingNow() || s.isInCombat() || s.isMovementDisabled())
				{
					player.sendPacket(SystemMessageId.A_SERVITOR_WHOM_IS_ENGAGED_IN_BATTLE_CANNOT_BE_DE_ACTIVATED);
					canUnsummon = false;
					break;
				}
			}
			
			if (canUnsummon)
			{
				final WorldObject target = player.getTarget();
				if ((target != null) && target.isSummon() && (target.asSummon().getOwner() == player) && !target.isPet())
				{
					target.asSummon().unSummon(player);
				}
				else
				{
					player.getServitors().values().forEach(s -> s.unSummon(player));
				}
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR);
		}
	}
}
