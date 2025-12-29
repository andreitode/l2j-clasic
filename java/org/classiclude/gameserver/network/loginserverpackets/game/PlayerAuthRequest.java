package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;
import org.classiclude.gameserver.LoginServerThread.SessionKey;

/**
 * @author -Wooden-
 */
public class PlayerAuthRequest extends BaseWritablePacket
{
	public PlayerAuthRequest(String account, SessionKey key)
	{
		writeByte(0x05);
		writeString(account);
		writeInt(key.playOkID1);
		writeInt(key.playOkID2);
		writeInt(key.loginOkID1);
		writeInt(key.loginOkID2);
	}
}