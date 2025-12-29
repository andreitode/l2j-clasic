package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author UnAfraid
 */
public class ChangePassword extends BaseWritablePacket
{
	public ChangePassword(String accountName, String characterName, String oldPass, String newPass)
	{
		writeByte(0x0B);
		writeString(accountName);
		writeString(characterName);
		writeString(oldPass);
		writeString(newPass);
	}
}