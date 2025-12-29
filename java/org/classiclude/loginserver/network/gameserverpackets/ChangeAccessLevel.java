package org.classiclude.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;
import org.classiclude.loginserver.LoginController;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(ChangeAccessLevel.class.getName());
	
	public ChangeAccessLevel(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final int level = readInt();
		final String account = readString();
		LoginController.getInstance().setAccountAccessLevel(account, level);
		LOGGER.info("Changed " + account + " access level to " + level);
	}
}
