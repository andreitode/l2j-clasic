package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;
import org.classiclude.loginserver.GameServerTable;

/**
 * @author -Wooden-
 */
public class AuthResponse extends BaseWritablePacket
{
	public AuthResponse(int serverId)
	{
		writeByte(0x02);
		writeByte(serverId);
		writeString(GameServerTable.getInstance().getServerNameById(serverId));
	}
}
