package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;

/**
 * @author -Wooden-
 */
public class PlayerAuthResponse extends BaseReadablePacket
{
	private final String _account;
	private final boolean _authed;
	
	public PlayerAuthResponse(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_account = readString();
		_authed = readByte() != 0;
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public boolean isAuthed()
	{
		return _authed;
	}
}