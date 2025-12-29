package org.classiclude.loginserver.network.loginserverpackets;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class LoginServerFail extends BaseWritablePacket
{
	public static final int REASON_IP_BANNED = 1;
	public static final int REASON_IP_RESERVED = 2;
	public static final int REASON_WRONG_HEXID = 3;
	public static final int REASON_ID_RESERVED = 4;
	public static final int REASON_NO_FREE_ID = 5;
	public static final int NOT_AUTHED = 6;
	public static final int REASON_ALREADY_LOGGED8IN = 7;
	
	public LoginServerFail(int reason)
	{
		writeByte(0x01);
		writeByte(reason);
	}
}
