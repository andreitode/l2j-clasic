package org.classiclude.gameserver.network.serverpackets.sayune;

import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.SayuneType;
import org.classiclude.gameserver.model.SayuneEntry;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExFlyMove extends ServerPacket
{
	private final int _objectId;
	private final SayuneType _type;
	private final int _mapId;
	private final List<SayuneEntry> _locations;
	
	public ExFlyMove(Player player, SayuneType type, int mapId, List<SayuneEntry> locations)
	{
		_objectId = player.getObjectId();
		_type = type;
		_mapId = mapId;
		_locations = locations;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FLY_MOVE.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_type.ordinal());
		buffer.writeInt(0); // ??
		buffer.writeInt(_mapId);
		buffer.writeInt(_locations.size());
		for (SayuneEntry loc : _locations)
		{
			buffer.writeInt(loc.getId());
			buffer.writeInt(0); // ??
			buffer.writeInt(loc.getX());
			buffer.writeInt(loc.getY());
			buffer.writeInt(loc.getZ());
		}
	}
}
