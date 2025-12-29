package org.classiclude.loginserver.network.gameserverpackets;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;

/**
 * @author -Wooden-
 */
public class PlayerInGame extends BaseReadablePacket
{
	public PlayerInGame(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final int size = readShort();
		for (int i = 0; i < size; i++)
		{
			final String account = readString();
			server.addAccountOnGameServer(account);
		}
	}
}
