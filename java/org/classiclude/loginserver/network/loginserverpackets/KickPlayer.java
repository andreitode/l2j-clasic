package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class KickPlayer extends BaseWritablePacket
{
	public KickPlayer(String account)
	{
		writeByte(0x04);
		writeString(account);
	}
}
