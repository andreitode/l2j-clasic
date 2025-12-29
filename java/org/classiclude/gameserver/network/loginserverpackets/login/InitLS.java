package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;

public class InitLS extends BaseReadablePacket
{
	private final int _rev;
	private final byte[] _key;
	
	public int getRevision()
	{
		return _rev;
	}
	
	public byte[] getRSAKey()
	{
		return _key;
	}
	
	public InitLS(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_rev = readInt();
		final int size = readInt();
		_key = readBytes(size);
	}
}