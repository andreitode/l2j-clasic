/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.classiclude.gameserver.network.clientpackets;

import org.classiclude.commons.network.ReadablePacket;
import org.classiclude.commons.util.CommonUtil;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;

/**
 * @author Mobius
 */
public abstract class ClientPacket extends ReadablePacket<GameClient>
{
	@Override
	public boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (Exception e)
		{
			PacketLogger.warning("Client: " + getClient() + " - Failed reading: " + getClass().getSimpleName() + " ; " + e.getMessage());
			PacketLogger.warning(CommonUtil.getStackTrace(e));
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
		}
		catch (Exception e)
		{
			PacketLogger.warning("Client: " + getClient() + " - Failed running: " + getClass().getSimpleName() + " ; " + e.getMessage());
			PacketLogger.warning(CommonUtil.getStackTrace(e));
			
			// In case of EnterWorld error kick player from game.
			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}
	
	protected abstract void runImpl();
	
	/**
	 * @return the active player if exist, otherwise null.
	 */
	protected Player getPlayer()
	{
		return getClient().getPlayer();
	}
}
