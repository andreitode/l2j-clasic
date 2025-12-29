package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;

/**
 * @author -Wooden-
 */
public class AuthResponse extends BaseReadablePacket
{
	private final int _serverId;
	private final String _serverName;
	
	public AuthResponse(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_serverId = readByte();
		_serverName = readString();
	}
	
	public int getServerId()
	{
		return _serverId;
	}
	
	public String getServerName()
	{
		return _serverName;
	}
}
