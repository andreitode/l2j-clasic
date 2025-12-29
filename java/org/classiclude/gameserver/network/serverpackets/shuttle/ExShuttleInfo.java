package org.classiclude.gameserver.network.serverpackets.shuttle;

import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.actor.instance.Shuttle;
import org.classiclude.gameserver.model.shuttle.ShuttleStop;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleInfo extends ServerPacket
{
	private final Shuttle _shuttle;
	private final List<ShuttleStop> _stops;
	
	public ExShuttleInfo(Shuttle shuttle)
	{
		_shuttle = shuttle;
		_stops = shuttle.getStops();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHUTTLE_INFO.writeId(this, buffer);
		buffer.writeInt(_shuttle.getObjectId());
		buffer.writeInt(_shuttle.getX());
		buffer.writeInt(_shuttle.getY());
		buffer.writeInt(_shuttle.getZ());
		buffer.writeInt(_shuttle.getHeading());
		buffer.writeInt(_shuttle.getId());
		buffer.writeInt(_stops.size());
		for (ShuttleStop stop : _stops)
		{
			buffer.writeInt(stop.getId());
			for (Location loc : stop.getDimensions())
			{
				buffer.writeInt(loc.getX());
				buffer.writeInt(loc.getY());
				buffer.writeInt(loc.getZ());
			}
			buffer.writeInt(stop.isDoorOpen());
			buffer.writeInt(stop.hasDoorChanged());
		}
	}
}
