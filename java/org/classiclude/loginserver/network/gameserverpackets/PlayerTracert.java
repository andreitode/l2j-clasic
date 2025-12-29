package org.classiclude.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.LoginController;

/**
 * @author mrTJO
 */
public class PlayerTracert extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(PlayerTracert.class.getName());
	
	public PlayerTracert(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final String account = readString();
		final String pcIp = readString();
		final String hop1 = readString();
		final String hop2 = readString();
		final String hop3 = readString();
		final String hop4 = readString();
		LoginController.getInstance().setAccountLastTracert(account, pcIp, hop1, hop2, hop3, hop4);
	}
}
