package org.classiclude.gameserver.network.serverpackets.friend;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * Support for "Chat with Friends" dialog. <br />
 * Inform player about friend online status change
 * @author JIV
 */
public class FriendStatus extends ServerPacket
{
	public static final int MODE_OFFLINE = 0;
	public static final int MODE_ONLINE = 1;
	public static final int MODE_LEVEL = 2;
	public static final int MODE_CLASS = 3;
	
	private final int _type;
	private final int _objectId;
	private final int _classId;
	private final int _level;
	private final String _name;
	
	public FriendStatus(Player player, int type)
	{
		_objectId = player.getObjectId();
		_classId = player.getActiveClass();
		_level = player.getLevel();
		_name = player.getName();
		_type = type;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_STATUS.writeId(this, buffer);
		buffer.writeInt(_type);
		buffer.writeString(_name);
		switch (_type)
		{
			case MODE_OFFLINE:
			{
				buffer.writeInt(_objectId);
				break;
			}
			case MODE_LEVEL:
			{
				buffer.writeInt(_level);
				break;
			}
			case MODE_CLASS:
			{
				buffer.writeInt(_classId);
				break;
			}
		}
	}
}
