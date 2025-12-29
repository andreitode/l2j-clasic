package org.classiclude.gameserver.network.serverpackets.friend;

import java.util.Calendar;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Sdw
 */
public class ExFriendDetailInfo extends ServerPacket
{
	private final int _objectId;
	private final Player _friend;
	private final String _name;
	private final int _lastAccess;
	
	public ExFriendDetailInfo(Player player, String name)
	{
		_objectId = player.getObjectId();
		_name = name;
		_friend = World.getInstance().getPlayer(_name);
		_lastAccess = _friend.isBlocked(player) ? 0 : _friend.isOnline() ? (int) System.currentTimeMillis() : (int) (System.currentTimeMillis() - _friend.getLastAccess()) / 1000;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FRIEND_DETAIL_INFO.writeId(this, buffer);
		buffer.writeInt(_objectId);
		if (_friend == null)
		{
			final int charId = CharInfoTable.getInstance().getIdByName(_name);
			buffer.writeString(_name);
			buffer.writeInt(0); // isonline = 0
			buffer.writeInt(charId);
			buffer.writeShort(CharInfoTable.getInstance().getLevelById(charId));
			buffer.writeShort(CharInfoTable.getInstance().getClassIdById(charId));
			final Clan clan = ClanTable.getInstance().getClan(CharInfoTable.getInstance().getClanIdById(charId));
			if (clan != null)
			{
				buffer.writeInt(clan.getId());
				buffer.writeInt(clan.getCrestId());
				buffer.writeString(clan.getName());
				buffer.writeInt(clan.getAllyId());
				buffer.writeInt(clan.getAllyCrestId());
				buffer.writeString(clan.getAllyName());
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeString("");
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeString("");
			}
			final Calendar createDate = CharInfoTable.getInstance().getCharacterCreationDate(charId);
			buffer.writeByte(createDate.get(Calendar.MONTH) + 1);
			buffer.writeByte(createDate.get(Calendar.DAY_OF_MONTH));
			buffer.writeInt(CharInfoTable.getInstance().getLastAccessDelay(charId));
			buffer.writeString(CharInfoTable.getInstance().getFriendMemo(_objectId, charId));
		}
		else
		{
			buffer.writeString(_friend.getName());
			buffer.writeInt(_friend.isOnlineInt());
			buffer.writeInt(_friend.getObjectId());
			buffer.writeShort(_friend.getLevel());
			buffer.writeShort(_friend.getClassId().getId());
			buffer.writeInt(_friend.getClanId());
			buffer.writeInt(_friend.getClanCrestId());
			buffer.writeString(_friend.getClan() != null ? _friend.getClan().getName() : "");
			buffer.writeInt(_friend.getAllyId());
			buffer.writeInt(_friend.getAllyCrestId());
			buffer.writeString(_friend.getClan() != null ? _friend.getClan().getAllyName() : "");
			final Calendar createDate = _friend.getCreateDate();
			buffer.writeByte(createDate.get(Calendar.MONTH) + 1);
			buffer.writeByte(createDate.get(Calendar.DAY_OF_MONTH));
			buffer.writeInt(_lastAccess);
			buffer.writeString(CharInfoTable.getInstance().getFriendMemo(_objectId, _friend.getObjectId()));
		}
	}
}
