package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.network.LoginClient;

/**
 * Format: d d: response
 */
public class GGAuth extends LoginServerPacket
{
	private final int _response;
	
	public GGAuth(int response)
	{
		_response = response;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x0b);
		buffer.writeInt(_response);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
