package org.classiclude.gameserver.network.serverpackets.primeshop;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Gnacik, UnAfraid
 */
public class ExBRGamePoint extends ServerPacket
{
	private final int _charId;
	private final int _charPoints;
	
	public ExBRGamePoint(Player player)
	{
		_charId = player.getObjectId();
		_charPoints = player.getPrimePoints();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_GAME_POINT.writeId(this, buffer);
		buffer.writeInt(_charId);
		buffer.writeLong(_charPoints);
		buffer.writeInt(0);
	}
}
