package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends BaseWritablePacket
{
	public ChangeAccessLevel(String player, int access)
	{
		writeByte(0x04);
		writeInt(access);
		writeString(player);
	}
}