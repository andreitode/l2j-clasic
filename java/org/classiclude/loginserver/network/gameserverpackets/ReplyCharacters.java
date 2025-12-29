package org.classiclude.loginserver.network.gameserverpackets;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;
import org.classiclude.loginserver.LoginController;

/**
 * Thanks to mochitto.
 * @author mrTJO
 */
public class ReplyCharacters extends BaseReadablePacket
{
	public ReplyCharacters(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final String account = readString();
		final int chars = readByte();
		final int charsToDel = readByte();
		final long[] charsList = new long[charsToDel];
		for (int i = 0; i < charsToDel; i++)
		{
			charsList[i] = readLong();
		}
		LoginController.getInstance().setCharactersOnServer(account, chars, charsList, server.getServerId());
	}
}
