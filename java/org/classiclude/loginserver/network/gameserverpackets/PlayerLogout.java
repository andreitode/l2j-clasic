package org.classiclude.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;

/**
 * @author -Wooden-
 */
public class PlayerLogout extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(PlayerLogout.class.getName());
	
	public PlayerLogout(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final String account = readString();
		server.removeAccountOnGameServer(account);
	}
}
