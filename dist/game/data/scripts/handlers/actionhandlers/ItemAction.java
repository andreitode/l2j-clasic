/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package handlers.actionhandlers;

import org.classiclude.gameserver.ai.CtrlIntention;
import org.classiclude.gameserver.enums.InstanceType;
import org.classiclude.gameserver.handler.IActionHandler;
import org.classiclude.gameserver.instancemanager.CastleManager;
import org.classiclude.gameserver.instancemanager.SiegeGuardManager;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.ClanPrivilege;
import org.classiclude.gameserver.model.siege.Castle;
import org.classiclude.gameserver.network.SystemMessageId;

public class ItemAction implements IActionHandler
{
	@Override
	public boolean action(Player player, WorldObject target, boolean interact)
	{
		final Castle castle = CastleManager.getInstance().getCastle(target);
		if ((castle != null) && (SiegeGuardManager.getInstance().getSiegeGuardByItem(castle.getResidenceId(), target.getId()) != null) && ((player.getClan() == null) || (castle.getOwnerId() != player.getClanId()) || !player.hasClanPrivilege(ClanPrivilege.CS_MERCENARIES)))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING);
			player.setTarget(target);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return false;
		}
		
		if (!player.isFlying())
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, target);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.Item;
	}
}