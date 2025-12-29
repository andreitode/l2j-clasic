package org.classiclude.loginserver.network.clientpackets;

import org.classiclude.loginserver.enums.LoginFailReason;
import org.classiclude.loginserver.network.ConnectionState;
import org.classiclude.loginserver.network.serverpackets.GGAuth;

/**
 * Format: ddddd
 * @author -Wooden-
 */
public class AuthGameGuard extends LoginClientPacket
{
	private int _sessionId;
	
	@Override
	protected boolean readImpl()
	{
		if (remaining() >= 20)
		{
			_sessionId = readInt();
			readInt(); // data1
			readInt(); // data2
			readInt(); // data3
			readInt(); // data4
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		if (_sessionId == getClient().getSessionId())
		{
			getClient().setConnectionState(ConnectionState.AUTHED_GG);
			getClient().sendPacket(new GGAuth(getClient().getSessionId()));
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
