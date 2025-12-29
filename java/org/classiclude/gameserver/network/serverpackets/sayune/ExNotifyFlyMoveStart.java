package org.classiclude.gameserver.network.serverpackets.sayune;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExNotifyFlyMoveStart extends ServerPacket
{
	public static final ExNotifyFlyMoveStart STATIC_PACKET = new ExNotifyFlyMoveStart();
	
	private ExNotifyFlyMoveStart()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NOTIFY_FLY_MOVE_START.writeId(this, buffer);
	}
}
