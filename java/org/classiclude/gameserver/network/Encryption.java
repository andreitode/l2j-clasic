package org.classiclude.gameserver.network;

import org.classiclude.commons.network.Buffer;

/**
 * @author KenM
 */
public class Encryption
{
	private final byte[] _inKey = new byte[16];
	private final byte[] _outKey = new byte[16];
	private boolean _isEnabled;
	
	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, _inKey, 0, 16);
		System.arraycopy(key, 0, _outKey, 0, 16);
	}
	
	public void encrypt(Buffer data, int offset, int size)
	{
		if (!_isEnabled)
		{
			_isEnabled = true;
			return;
		}
		
		int encrypted = 0;
		for (int i = 0; i < size; i++)
		{
			final int raw = Byte.toUnsignedInt(data.readByte(offset + i));
			encrypted = raw ^ _outKey[i & 0x0f] ^ encrypted;
			data.writeByte(offset + i, (byte) encrypted);
		}
		
		// Shift key.
		int old = _outKey[8] & 0xff;
		old |= (_outKey[9] << 8) & 0xff00;
		old |= (_outKey[10] << 16) & 0xff0000;
		old |= (_outKey[11] << 24) & 0xff000000;
		old += size;
		_outKey[8] = (byte) (old & 0xff);
		_outKey[9] = (byte) ((old >> 8) & 0xff);
		_outKey[10] = (byte) ((old >> 16) & 0xff);
		_outKey[11] = (byte) ((old >> 24) & 0xff);
	}
	
	public void decrypt(Buffer data, int offset, int size)
	{
		if (!_isEnabled)
		{
			return;
		}
		
		int xOr = 0;
		for (int i = 0; i < size; i++)
		{
			final int encrypted = Byte.toUnsignedInt(data.readByte(offset + i));
			data.writeByte(offset + i, (byte) (encrypted ^ _inKey[i & 15] ^ xOr));
			xOr = encrypted;
		}
		
		// Shift key.
		int old = _inKey[8] & 0xff;
		old |= (_inKey[9] << 8) & 0xff00;
		old |= (_inKey[10] << 16) & 0xff0000;
		old |= (_inKey[11] << 24) & 0xff000000;
		old += size;
		_inKey[8] = (byte) (old & 0xff);
		_inKey[9] = (byte) ((old >> 8) & 0xff);
		_inKey[10] = (byte) ((old >> 16) & 0xff);
		_inKey[11] = (byte) ((old >> 24) & 0xff);
	}
}
