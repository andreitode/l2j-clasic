package org.classiclude.loginserver.network.clientpackets;

import org.classiclude.Config;
import org.classiclude.loginserver.network.serverpackets.PIAgreementCheck;

/**
 * @author UnAfraid
 */
public class RequestPIAgreementCheck extends LoginClientPacket
{
	private int _accountId;
	
	@Override
	protected boolean readImpl()
	{
		_accountId = readInt();
		final byte[] padding0 = new byte[3];
		final byte[] checksum = new byte[4];
		final byte[] padding1 = new byte[12];
		readBytes(padding0);
		readBytes(checksum);
		readBytes(padding1);
		return true;
	}
	
	@Override
	public void run()
	{
		getClient().sendPacket(new PIAgreementCheck(_accountId, Config.SHOW_PI_AGREEMENT ? 0x01 : 0x00));
	}
}
