package org.classiclude.loginserver.network.clientpackets;

import org.classiclude.loginserver.network.serverpackets.PIAgreementAck;

/**
 * @author UnAfraid
 */
public class RequestPIAgreement extends LoginClientPacket
{
	private int _accountId;
	private int _status;
	
	@Override
	protected boolean readImpl()
	{
		_accountId = readInt();
		_status = readByte();
		return true;
	}
	
	@Override
	public void run()
	{
		getClient().sendPacket(new PIAgreementAck(_accountId, _status));
	}
}
