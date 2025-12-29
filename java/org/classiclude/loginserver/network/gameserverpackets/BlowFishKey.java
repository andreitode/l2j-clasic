package org.classiclude.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.classiclude.commons.crypt.NewCrypt;
import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;
import org.classiclude.loginserver.network.GameServerPacketHandler.GameServerState;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(BlowFishKey.class.getName());
	
	public BlowFishKey(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final int size = readInt();
		final byte[] tempKey = readBytes(size);
		try
		{
			byte[] tempDecryptKey;
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, server.getPrivateKey());
			tempDecryptKey = rsaCipher.doFinal(tempKey);
			// There are nulls before the key we must remove them.
			int i = 0;
			final int len = tempDecryptKey.length;
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
				{
					break;
				}
			}
			final byte[] key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, key, 0, len - i);
			server.setBlowFish(new NewCrypt(key));
			server.setLoginConnectionState(GameServerState.BF_CONNECTED);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.log(Level.SEVERE, "Error While decrypting blowfish key (RSA): " + e.getMessage(), e);
		}
	}
}
