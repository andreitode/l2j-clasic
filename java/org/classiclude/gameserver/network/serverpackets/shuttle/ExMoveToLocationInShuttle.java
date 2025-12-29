package org.classiclude.gameserver.network.serverpackets.shuttle;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExMoveToLocationInShuttle extends ServerPacket
{
	private final int _objectId;
	private final int _airShipId;
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;
	private final int _fromX;
	private final int _fromY;
	private final int _fromZ;
	
	public ExMoveToLocationInShuttle(Player player, int fromX, int fromY, int fromZ)
	{
		_objectId = player.getObjectId();
		_airShipId = player.getShuttle().getObjectId();
		_targetX = player.getInVehiclePosition().getX();
		_targetY = player.getInVehiclePosition().getY();
		_targetZ = player.getInVehiclePosition().getZ();
		_fromX = fromX;
		_fromY = fromY;
		_fromZ = fromZ;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MOVE_TO_LOCATION_IN_SUTTLE.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_airShipId);
		buffer.writeInt(_targetX);
		buffer.writeInt(_targetY);
		buffer.writeInt(_targetZ);
		buffer.writeInt(_fromX);
		buffer.writeInt(_fromY);
		buffer.writeInt(_fromZ);
	}
}
