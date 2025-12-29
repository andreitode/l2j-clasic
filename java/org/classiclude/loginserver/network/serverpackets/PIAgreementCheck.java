package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @author UnAfraid
 */
public class PIAgreementCheck extends LoginServerPacket
{
	private final int _accountId;
	private final int _status;
	
	public PIAgreementCheck(int accountId, int status)
	{
		_accountId = accountId;
		_status = status;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x11);
		buffer.writeInt(_accountId);
		buffer.writeByte(_status);
	}
}
