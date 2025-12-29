package org.classiclude.loginserver.network.clientpackets;

import org.classiclude.loginserver.enums.LoginFailReason;
import org.classiclude.loginserver.network.LoginClient;
import org.classiclude.loginserver.network.serverpackets.ServerList;

/**
 * <pre>
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 * </pre>
 */
public class RequestServerList extends LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	@SuppressWarnings("unused")
	private int _data3;
	
	@Override
	protected boolean readImpl()
	{
		if (remaining() >= 8)
		{
			_skey1 = readInt(); // loginOk 1
			_skey2 = readInt(); // loginOk 2
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final LoginClient client = getClient();
		if (client.getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			client.sendPacket(new ServerList(client));
		}
		else
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
