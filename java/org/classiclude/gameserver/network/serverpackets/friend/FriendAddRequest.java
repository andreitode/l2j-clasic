package org.classiclude.gameserver.network.serverpackets.friend;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Mobius
 */
public class FriendAddRequest extends ServerPacket
{
	private final String _requestorName;
	
	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_ADD_REQUEST.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeString(_requestorName);
	}
}
