package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.enums.AccountKickedReason;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @author KenM
 */
public class AccountKicked extends LoginServerPacket
{
	private final AccountKickedReason _reason;
	
	public AccountKicked(AccountKickedReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x02);
		buffer.writeInt(_reason.getCode());
	}
}

