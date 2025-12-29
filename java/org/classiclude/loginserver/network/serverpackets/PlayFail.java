package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.enums.PlayFailReason;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:11 $
 */
public class PlayFail extends LoginServerPacket
{
	private final PlayFailReason _reason;
	
	public PlayFail(PlayFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x06);
		buffer.writeByte(_reason.getCode());
	}
}
