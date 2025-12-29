package org.classiclude.gameserver.network.serverpackets.friend;

import java.util.LinkedList;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * Support for "Chat with Friends" dialog. <br />
 * This packet is sent only at login.
 * @author Tempy
 */
public class L2FriendList extends ServerPacket
{
	private final List<FriendInfo> _info = new LinkedList<>();
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		int _level;
		int _classId;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online, int level, int classId)
		{
			_objId = objId;
			_name = name;
			_online = online;
			_level = level;
			_classId = classId;
		}
	}
	
	public L2FriendList(Player player)
	{
		for (int objId : player.getFriendList())
		{
			final String name = CharInfoTable.getInstance().getNameById(objId);
			final Player player1 = World.getInstance().getPlayer(objId);
			boolean online = false;
			int level = 0;
			int classId = 0;
			if (player1 != null)
			{
				online = true;
				level = player1.getLevel();
				classId = player1.getClassId().getId();
			}
			else
			{
				level = CharInfoTable.getInstance().getLevelById(objId);
				classId = CharInfoTable.getInstance().getClassIdById(objId);
			}
			_info.add(new FriendInfo(objId, name, online, level, classId));
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.L2_FRIEND_LIST.writeId(this, buffer);
		buffer.writeInt(_info.size());
		for (FriendInfo info : _info)
		{
			buffer.writeInt(info._objId); // character id
			buffer.writeString(info._name);
			buffer.writeInt(info._online); // online
			buffer.writeInt(info._online ? info._objId : 0); // object id if online
			buffer.writeInt(info._level);
			buffer.writeInt(info._classId);
			buffer.writeShort(0);
		}
	}
}