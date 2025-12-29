package org.classiclude.gameserver.network.serverpackets.sayune;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.SayuneType;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.interfaces.ILocational;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExFlyMoveBroadcast extends ServerPacket
{
	private final int _objectId;
	private final int _mapId;
	private final ILocational _currentLoc;
	private final ILocational _targetLoc;
	private final SayuneType _type;
	
	public ExFlyMoveBroadcast(Player player, SayuneType type, int mapId, ILocational targetLoc)
	{
		_objectId = player.getObjectId();
		_type = type;
		_mapId = mapId;
		_currentLoc = player;
		_targetLoc = targetLoc;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FLY_MOVE_BROADCAST.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_type.ordinal());
		buffer.writeInt(_mapId);
		buffer.writeInt(_targetLoc.getX());
		buffer.writeInt(_targetLoc.getY());
		buffer.writeInt(_targetLoc.getZ());
		buffer.writeInt(0); // ?
		buffer.writeInt(_currentLoc.getX());
		buffer.writeInt(_currentLoc.getY());
		buffer.writeInt(_currentLoc.getZ());
	}
}
