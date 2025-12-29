package org.classiclude.loginserver.network;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author Mobius
 */
public enum LoginServerPackets
{
	INIT(0x00),
	LOGIN_FAIL(0x01),
	ACCOUNT_KICKED(0x02),
	LOGIN_OK(0x03),
	SERVER_LIST(0x04),
	PLAY_FAIL(0x06),
	PLAY_OK(0x07),
	
	PI_AGREEMENT_CHECK(0x11),
	PI_AGREEMENT_ACK(0x12),
	GG_AUTH(0x0b),
	LOGIN_OPT_FAIL(0x0D);
	
	private final int _id;
	
	LoginServerPackets(int id)
	{
		_id = id;
	}
	
	public void writeId(BaseWritablePacket packet)
	{
		packet.writeByte(_id);
	}
}
