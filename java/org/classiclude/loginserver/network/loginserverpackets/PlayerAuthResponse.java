package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class PlayerAuthResponse extends BaseWritablePacket
{
	public PlayerAuthResponse(String account, boolean response)
	{
		writeByte(0x03);
		writeString(account);
		writeByte(response ? 1 : 0);
	}
}
