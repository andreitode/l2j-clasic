package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author mrTJO
 */
public class TempBan extends BaseWritablePacket
{
	public TempBan(String accountName, String ip, long time)
	{
		writeByte(0x0A);
		writeString(accountName);
		writeString(ip);
		writeLong(System.currentTimeMillis() + (time * 60000));
		// if (reason != null)
		// {
		// writeByte(0x01);
		// writeString(reason);
		// }
		// else
		// {
		writeByte(0x00);
		// }
	}
}
