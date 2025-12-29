package org.classiclude.gameserver.network.serverpackets.shuttle;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.instance.Shuttle;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleGetOn extends ServerPacket
{
	private final int _playerObjectId;
	private final int _shuttleObjectId;
	private final Location _pos;
	
	public ExShuttleGetOn(Player player, Shuttle shuttle)
	{
		_playerObjectId = player.getObjectId();
		_shuttleObjectId = shuttle.getObjectId();
		_pos = player.getInVehiclePosition();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUTTLE_GET_ON.writeId(this, buffer);
		buffer.writeInt(_playerObjectId);
		buffer.writeInt(_shuttleObjectId);
		buffer.writeInt(_pos.getX());
		buffer.writeInt(_pos.getY());
		buffer.writeInt(_pos.getZ());
	}
}
