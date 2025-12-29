package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author Nik
 */
public class ChangePasswordResponse extends BaseWritablePacket
{
	public ChangePasswordResponse(String characterName, String msgToSend)
	{
		writeByte(0x06);
		writeString(characterName);
		writeString(msgToSend);
	}
}