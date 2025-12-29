package org.classiclude.gameserver.network.loginserverpackets.game;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author mrTJO
 */
public class PlayerTracert extends BaseWritablePacket
{
	public PlayerTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4)
	{
		writeByte(0x07);
		writeString(account);
		writeString(pcIp);
		writeString(hop1);
		writeString(hop2);
		writeString(hop3);
		writeString(hop4);
	}
}