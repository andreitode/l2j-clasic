package org.classiclude.gameserver.network.serverpackets.pledgebonus;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExPledgeBonusOpen extends ServerPacket
{
	private final Player _player;
	
	public ExPledgeBonusOpen(Player player)
	{
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		final Clan clan = _player.getClan();
		if (clan == null)
		{
			PacketLogger.warning("Player: " + _player + " attempting to write to a null clan!");
			return;
		}
		
		// General OP Code
		
	}
}
