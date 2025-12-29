package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.network.LoginClient;

/**
 * <pre>
 * Format: dd b dddd s
 * d: session id
 * d: protocol revision
 * b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key
 *                 0x10 bytes at 0x00
 * d: unknow
 * d: unknow
 * d: unknow
 * d: unknow
 * s: blowfish key
 * </pre>
 */
public class Init extends LoginServerPacket
{
	private final int _sessionId;
	
	private final byte[] _publicKey;
	private final byte[] _blowfishKey;
	
	public Init(LoginClient client)
	{
		this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
	}
	
	public Init(byte[] publickey, byte[] blowfishkey, int sessionId)
	{
		_sessionId = sessionId;
		_publicKey = publickey;
		_blowfishKey = blowfishkey;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x00); // Init packet id.
		
		buffer.writeInt(_sessionId); // Session id.
		buffer.writeInt(0x0000c621); // Protocol revision.
		
		buffer.writeBytes(_publicKey); // RSA Public Key.
		
		// GG related.
		buffer.writeInt(0x29DD954E);
		buffer.writeInt(0x77C39CFC);
		buffer.writeInt(0x97ADB620);
		buffer.writeInt(0x07BDE0F7);
		
		buffer.writeBytes(_blowfishKey); // BlowFish key.
		buffer.writeByte(0); // Null termination.
	}
}
