package org.classiclude.loginserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.classiclude.commons.network.ReadablePacket;
import org.classiclude.loginserver.network.LoginClient;

/**
 * @author KenM
 */
public abstract class LoginClientPacket extends ReadablePacket<LoginClient>
{
	private static final Logger LOGGER = Logger.getLogger(LoginClientPacket.class.getName());
	
	@Override
	protected boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "ERROR READING: " + getClass().getSimpleName() + ": " + e.getMessage(), e);
			return false;
		}
	}
	
	protected abstract boolean readImpl();
}
