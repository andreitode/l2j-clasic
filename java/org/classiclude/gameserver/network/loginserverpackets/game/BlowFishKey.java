package org.classiclude.gameserver.network.loginserverpackets.game;

import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends BaseWritablePacket
{
	private static final Logger LOGGER = Logger.getLogger(BlowFishKey.class.getName());
	
	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		writeByte(0x00);
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			final byte[] encrypted = rsaCipher.doFinal(blowfishKey);
			writeInt(encrypted.length);
			writeBytes(encrypted);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error While encrypting blowfish key for transmision (Crypt error): " + e.getMessage(), e);
		}
	}
}
