package org.classiclude.loginserver.network.clientpackets;

import org.classiclude.Config;
import org.classiclude.loginserver.LoginController;
import org.classiclude.loginserver.LoginServer;
import org.classiclude.loginserver.SessionKey;
import org.classiclude.loginserver.enums.LoginFailReason;
import org.classiclude.loginserver.enums.PlayFailReason;
import org.classiclude.loginserver.network.LoginClient;
import org.classiclude.loginserver.network.gameserverpackets.ServerStatus;
import org.classiclude.loginserver.network.serverpackets.PlayOk;

/**
 * <pre>
 * Format is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 * </pre>
 */
public class RequestServerLogin extends LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	@Override
	protected boolean readImpl()
	{
		if (remaining() >= 9)
		{
			_skey1 = readInt();
			_skey2 = readInt();
			_serverId = readByte();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final LoginClient client = getClient();
		final SessionKey sk = client.getSessionKey();
		
		// If we didn't showed the license we can't check these values.
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if ((LoginServer.getInstance().getStatus() == ServerStatus.STATUS_DOWN) || ((LoginServer.getInstance().getStatus() == ServerStatus.STATUS_GM_ONLY) && (client.getAccessLevel() < 1)))
			{
				client.close(LoginFailReason.REASON_ACCESS_FAILED);
			}
			else if (LoginController.getInstance().isLoginPossible(client, _serverId))
			{
				client.setJoinedGS(true);
				client.sendPacket(new PlayOk(sk));
			}
			else
			{
				client.close(PlayFailReason.REASON_SERVER_OVERLOADED);
			}
		}
		else
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
