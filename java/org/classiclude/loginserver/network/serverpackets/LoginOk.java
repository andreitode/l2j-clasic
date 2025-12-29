package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.SessionKey;
import org.classiclude.loginserver.network.LoginClient;

/**
 * <pre>
 * Format: dddddddd
 * f: the session key
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * d: ?
 * b: 16 bytes - unknown
 * </pre>
 */
public class LoginOk extends LoginServerPacket
{
	private final int _loginOk1;
	private final int _loginOk2;
	
	public LoginOk(SessionKey sessionKey)
	{
		_loginOk1 = sessionKey.loginOkID1;
		_loginOk2 = sessionKey.loginOkID2;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x03);
		buffer.writeInt(_loginOk1);
		buffer.writeInt(_loginOk2);
		buffer.writeInt(0x00);
		buffer.writeInt(0x00);
		buffer.writeInt(0x000003ea);
		buffer.writeInt(0x00);
		buffer.writeInt(0x00);
		buffer.writeInt(0x00);
		buffer.writeBytes(new byte[16]);
	}
}
