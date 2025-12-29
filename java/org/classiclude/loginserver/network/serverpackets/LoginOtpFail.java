package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @author UnAfraid
 */
public class LoginOtpFail extends LoginServerPacket
{
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x0D);
	}
}
