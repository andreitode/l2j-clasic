package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;

public class LoginServerFail extends BaseReadablePacket
{
	private static final String[] REASONS =
	{
		"None",
		"Reason: ip banned",
		"Reason: ip reserved",
		"Reason: wrong hexid",
		"Reason: id reserved",
		"Reason: no free ID",
		"Not authed",
		"Reason: already logged in"
	};
	private final int _reason;
	
	public LoginServerFail(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_reason = readByte();
	}
	
	public String getReasonString()
	{
		return REASONS[_reason];
	}
	
	public int getReason()
	{
		return _reason;
	}
}