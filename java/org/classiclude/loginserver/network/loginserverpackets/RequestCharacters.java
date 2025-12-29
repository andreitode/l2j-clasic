package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author mrTJO
 */
public class RequestCharacters extends BaseWritablePacket
{
	public RequestCharacters(String account)
	{
		writeByte(0x05);
		writeString(account);
	}
}
