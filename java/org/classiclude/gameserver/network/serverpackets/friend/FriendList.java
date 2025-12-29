package org.classiclude.gameserver.network.serverpackets.friend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * Support for "Chat with Friends" dialog. <br />
 * This packet is sent only at login.
 * @author mrTJO, UnAfraid
 */
public class FriendList extends ServerPacket
{
	private final List<FriendInfo> _info = new LinkedList<>();
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		int _classid;
		int _level;
		
		public FriendInfo(int objId, String name, boolean online, int classid, int level)
		{
			_objId = objId;
			_name = name;
			_online = online;
			_classid = classid;
			_level = level;
		}
	}
	
	public FriendList(Player player)
	{
		for (int objId : player.getFriendList())
		{
			final String name = CharInfoTable.getInstance().getNameById(objId);
			final Player player1 = World.getInstance().getPlayer(objId);
			boolean online = false;
			int classid = 0;
			int level = 0;
			if (player1 == null)
			{
				try (Connection con = DatabaseFactory.getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT char_name, online, classid, level FROM characters WHERE charId = ?"))
				{
					statement.setInt(1, objId);
					try (ResultSet rset = statement.executeQuery())
					{
						if (rset.next())
						{
							_info.add(new FriendInfo(objId, rset.getString(1), rset.getInt(2) == 1, rset.getInt(3), rset.getInt(4)));
						}
					}
				}
				catch (Exception e)
				{
					// Who cares?
				}
				continue;
			}
			if (player1.isOnline())
			{
				online = true;
			}
			classid = player1.getClassId().getId();
			level = player1.getLevel();
			_info.add(new FriendInfo(objId, name, online, classid, level));
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_LIST.writeId(this, buffer);
		buffer.writeInt(_info.size());
		for (FriendInfo info : _info)
		{
			buffer.writeInt(info._objId); // character id
			buffer.writeString(info._name);
			buffer.writeInt(info._online); // online
			buffer.writeInt(info._online ? info._objId : 0); // object id if online
			buffer.writeInt(info._classid);
			buffer.writeInt(info._level);
		}
	}
}
