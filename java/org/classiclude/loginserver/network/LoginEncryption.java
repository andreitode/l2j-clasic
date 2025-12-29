package org.classiclude.loginserver.network;

import java.io.IOException;

import org.classiclude.commons.network.Buffer;
import org.classiclude.commons.util.Rnd;
import org.classiclude.loginserver.crypt.NewCrypt;

/**
 * @author KenM
 */
public class LoginEncryption
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};
	
	private static final NewCrypt _STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);
	
	private NewCrypt _crypt = null;
	private boolean _static = true;
	
	public void setKey(byte[] key)
	{
		_crypt = new NewCrypt(key);
	}
	
	public boolean decrypt(Buffer data, final int offset, final int size) throws IOException
	{
		_crypt.decrypt(data, offset, size);
		return NewCrypt.verifyChecksum(data, offset, size);
	}
	
	public int encryptedSize(int dataSize)
	{
		dataSize += _static ? 8 : 4;
		dataSize += 8 - (dataSize % 8);
		dataSize += 8;
		return dataSize;
	}
	
	public boolean encrypt(Buffer data, final int offset, int size) throws IOException
	{
		final int encryptedSize = offset + encryptedSize(size);
		data.limit(encryptedSize);
		if (_static)
		{
			NewCrypt.encXORPass(data, offset, encryptedSize, Rnd.nextInt());
			_STATIC_CRYPT.crypt(data, offset, encryptedSize);
			_static = false;
		}
		else
		{
			NewCrypt.appendChecksum(data, offset, encryptedSize);
			_crypt.crypt(data, offset, encryptedSize);
		}
		return true;
	}
}
