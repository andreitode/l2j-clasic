package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;

public class KickPlayer extends BaseReadablePacket
{
	private final String _account;
	
	public KickPlayer(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_account = readString();
	}
	
	public String getAccount()
	{
		return _account;
	}
}