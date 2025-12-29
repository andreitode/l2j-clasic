package org.classiclude.gameserver.network.serverpackets.shuttle;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.instance.Shuttle;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleMove extends ServerPacket
{
	private final Shuttle _shuttle;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public ExShuttleMove(Shuttle shuttle, int x, int y, int z)
	{
		_shuttle = shuttle;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUTTLE_MOVE.writeId(this, buffer);
		buffer.writeInt(_shuttle.getObjectId());
		buffer.writeInt((int) _shuttle.getStat().getMoveSpeed());
		buffer.writeInt((int) _shuttle.getStat().getRotationSpeed());
		buffer.writeInt(_x);
		buffer.writeInt(_y);
		buffer.writeInt(_z);
	}
}
