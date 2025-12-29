package org.classiclude.gameserver.network.serverpackets.friend;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class FriendRemove extends ServerPacket
{
	private final int _responce;
	private final String _charName;
	
	public FriendRemove(String charName, int responce)
	{
		_responce = responce;
		_charName = charName;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_REMOVE.writeId(this, buffer);
		buffer.writeInt(_responce);
		buffer.writeString(_charName);
	}
}
