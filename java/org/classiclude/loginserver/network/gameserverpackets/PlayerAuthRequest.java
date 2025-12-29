package org.classiclude.loginserver.network.gameserverpackets;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;
import org.classiclude.loginserver.LoginController;
import org.classiclude.loginserver.SessionKey;
import org.classiclude.loginserver.network.loginserverpackets.PlayerAuthResponse;

/**
 * @author -Wooden-
 */
public class PlayerAuthRequest extends BaseReadablePacket
{
	public PlayerAuthRequest(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final String account = readString();
		final int playKey1 = readInt();
		final int playKey2 = readInt();
		final int loginKey1 = readInt();
		final int loginKey2 = readInt();
		
		final SessionKey sessionKey = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);
		final SessionKey key = LoginController.getInstance().getKeyForAccount(account);
		if ((key != null) && key.equals(sessionKey))
		{
			LoginController.getInstance().removeAuthedLoginClient(account);
			server.sendPacket(new PlayerAuthResponse(account, true));
		}
		else
		{
			server.sendPacket(new PlayerAuthResponse(account, false));
		}
	}
}
