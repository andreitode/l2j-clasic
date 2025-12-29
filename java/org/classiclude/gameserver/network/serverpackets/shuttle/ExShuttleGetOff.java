package org.classiclude.gameserver.network.serverpackets.shuttle;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.instance.Shuttle;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleGetOff extends ServerPacket
{
	private final int _playerObjectId;
	private final int _shuttleObjectId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public ExShuttleGetOff(Player player, Shuttle shuttle, int x, int y, int z)
	{
		_playerObjectId = player.getObjectId();
		_shuttleObjectId = shuttle.getObjectId();
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUTTLE_GET_OFF.writeId(this, buffer);
		buffer.writeInt(_playerObjectId);
		buffer.writeInt(_shuttleObjectId);
		buffer.writeInt(_x);
		buffer.writeInt(_y);
		buffer.writeInt(_z);
	}
}
