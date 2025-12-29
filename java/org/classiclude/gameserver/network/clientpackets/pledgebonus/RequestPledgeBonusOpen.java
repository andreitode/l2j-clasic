package org.classiclude.gameserver.network.clientpackets.pledgebonus;

import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.clientpackets.ClientPacket;
import org.classiclude.gameserver.network.serverpackets.pledgebonus.ExPledgeBonusOpen;

/**
 * @author UnAfraid
 */
public class RequestPledgeBonusOpen extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		player.sendPacket(new ExPledgeBonusOpen(player));
	}
}
