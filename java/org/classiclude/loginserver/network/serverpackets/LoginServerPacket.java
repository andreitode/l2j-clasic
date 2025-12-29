package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.commons.network.WritablePacket;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @author KenM
 */
public abstract class LoginServerPacket extends WritablePacket<LoginClient>
{
	// public static final Logger LOGGER = Logger.getLogger(LoginServerPacket.class.getName());
	
	@Override
	protected boolean write(LoginClient client, WritableBuffer buffer)
	{
		try
		{
			writeImpl(client, buffer);
			return true;
		}
		catch (Exception e)
		{
			// LOGGER.error(e.getMessage(), e);
		}
		return false;
	}
	
	protected abstract void writeImpl(LoginClient client, WritableBuffer buffer);
}
