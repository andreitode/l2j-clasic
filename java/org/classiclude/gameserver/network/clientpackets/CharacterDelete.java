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

import org.classiclude.gameserver.enums.CharacterDeleteFailType;
import org.classiclude.gameserver.model.CharSelectInfoPackage;
import org.classiclude.gameserver.model.events.Containers;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerDelete;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.serverpackets.CharDeleteFail;
import org.classiclude.gameserver.network.serverpackets.CharDeleteSuccess;
import org.classiclude.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * @version $Revision: 1.8.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterDelete extends ClientPacket
{
	// cd
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		
		// if (!client.getFloodProtectors().canSelectCharacter())
		// {
		// client.sendPacket(new CharDeleteFail(CharacterDeleteFailType.UNKNOWN));
		// return;
		// }
		
		try
		{
			final CharacterDeleteFailType failType = client.markToDeleteChar(_charSlot);
			switch (failType)
			{
				case NONE:// Success!
				{
					client.sendPacket(new CharDeleteSuccess());
					final CharSelectInfoPackage charInfo = client.getCharSelection(_charSlot);
					if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_DELETE, Containers.Players()))
					{
						EventDispatcher.getInstance().notifyEvent(new OnPlayerDelete(charInfo.getObjectId(), charInfo.getName(), client), Containers.Players());
					}
					break;
				}
				default:
				{
					client.sendPacket(new CharDeleteFail(failType));
					break;
				}
			}
		}
		catch (Exception e)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1, 0);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
