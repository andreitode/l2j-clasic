package org.classiclude.gameserver.network.serverpackets.friend;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * Support for "Chat with Friends" dialog. <br />
 * Add new friend or delete.
 * @author JIV
 */
public class L2Friend extends ServerPacket
{
	private final boolean _action;
	private final boolean _online;
	private final int _objid;
	private final String _name;
	
	/**
	 * @param action - true for adding, false for remove
	 * @param objId
	 */
	public L2Friend(boolean action, int objId)
	{
		_action = action;
		_objid = objId;
		_name = CharInfoTable.getInstance().getNameById(objId);
		_online = World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.L2_FRIEND.writeId(this, buffer);
		buffer.writeInt(_action ? 1 : 3); // 1-add 3-remove
		buffer.writeInt(_objid);
		buffer.writeString(_name);
		buffer.writeInt(_online);
		buffer.writeInt(_online ? _objid : 0);
	}
}
