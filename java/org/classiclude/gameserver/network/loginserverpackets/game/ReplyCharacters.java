package org.classiclude.gameserver.network.loginserverpackets.game;

import java.util.List;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author mrTJO, mochitto
 */
public class ReplyCharacters extends BaseWritablePacket
{
	public ReplyCharacters(String account, int chars, List<Long> timeToDel)
	{
		writeByte(0x08);
		writeString(account);
		writeByte(chars);
		writeByte(timeToDel.size());
		for (long time : timeToDel)
		{
			writeLong(time);
		}
	}
}
