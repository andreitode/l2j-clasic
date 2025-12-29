package org.classiclude.gameserver.network.serverpackets.friend;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class FriendAddRequestResult extends ServerPacket
{
	private final int _result;
	private final int _charId;
	private final String _charName;
	private final int _isOnline;
	private final int _charObjectId;
	private final int _charLevel;
	private final int _charClassId;
	
	public FriendAddRequestResult(Player player, int result)
	{
		_result = result;
		_charId = player.getObjectId();
		_charName = player.getName();
		_isOnline = player.isOnlineInt();
		_charObjectId = player.getObjectId();
		_charLevel = player.getLevel();
		_charClassId = player.getActiveClass();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_ADD_REQUEST_RESULT.writeId(this, buffer);
		buffer.writeInt(_result);
		buffer.writeInt(_charId);
		buffer.writeString(_charName);
		buffer.writeInt(_isOnline);
		buffer.writeInt(_charObjectId);
		buffer.writeInt(_charLevel);
		buffer.writeInt(_charClassId);
		buffer.writeShort(0); // Always 0 on retail
	}
}
