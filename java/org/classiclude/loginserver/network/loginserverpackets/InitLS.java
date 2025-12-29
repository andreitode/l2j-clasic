package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;
import org.classiclude.loginserver.LoginServer;

/**
 * @author -Wooden-
 */
public class InitLS extends BaseWritablePacket
{
	public InitLS(byte[] publickey)
	{
		writeByte(0x00);
		writeInt(LoginServer.PROTOCOL_REV);
		writeInt(publickey.length);
		writeBytes(publickey);
	}
}
