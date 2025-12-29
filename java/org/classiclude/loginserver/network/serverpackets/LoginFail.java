package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.enums.LoginFailReason;
import org.classiclude.loginserver.network.LoginClient;

/**
 * Format: d d: the failure reason
 */
public class LoginFail extends LoginServerPacket
{
	private final LoginFailReason _reason;
	
	public LoginFail(LoginFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x01);
		buffer.writeByte(_reason.getCode());
	}
}
