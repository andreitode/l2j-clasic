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
public class ExValidateLocationInShuttle extends ServerPacket
{
	private final Player _player;
	private final int _shipId;
	private final int _heading;
	private final Location _loc;
	
	public ExValidateLocationInShuttle(Player player)
	{
		_player = player;
		_shipId = _player.getShuttle().getObjectId();
		_loc = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VALIDATE_LOCATION_IN_SHUTTLE.writeId(this, buffer);
		buffer.writeInt(_player.getObjectId());
		buffer.writeInt(_shipId);
		buffer.writeInt(_loc.getX());
		buffer.writeInt(_loc.getY());
		buffer.writeInt(_loc.getZ());
		buffer.writeInt(_heading);
	}
}
