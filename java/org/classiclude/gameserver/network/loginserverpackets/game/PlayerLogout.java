package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class PlayerLogout extends BaseWritablePacket
{
	public PlayerLogout(String player)
	{
		writeByte(0x03);
		writeString(player);
	}
}