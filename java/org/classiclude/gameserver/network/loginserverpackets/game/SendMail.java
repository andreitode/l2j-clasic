package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author mrTJO
 */
public class SendMail extends BaseWritablePacket
{
	public SendMail(String accountName, String mailId, String... args)
	{
		writeByte(0x09);
		writeString(accountName);
		writeString(mailId);
		writeByte(args.length);
		for (String arg : args)
		{
			writeString(arg);
		}
	}
}
