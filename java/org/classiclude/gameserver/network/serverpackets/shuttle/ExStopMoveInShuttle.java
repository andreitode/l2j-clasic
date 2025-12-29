package org.classiclude.gameserver.network.serverpackets.shuttle;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExStopMoveInShuttle extends ServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final Location _pos;
	private final int _heading;
	
	public ExStopMoveInShuttle(Player player, int boatId)
	{
		_objectId = player.getObjectId();
		_boatId = boatId;
		_pos = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_STOP_MOVE_IN_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_boatId);
		buffer.writeInt(_pos.getX());
		buffer.writeInt(_pos.getY());
		buffer.writeInt(_pos.getZ());
		buffer.writeInt(_heading);
	}
}
