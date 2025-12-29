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
package org.classiclude.gameserver.network.serverpackets;

import java.util.Collection;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.instancemanager.CastleManager;
import org.classiclude.gameserver.model.siege.Castle;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExShowCastleInfo extends ServerPacket
{
	public ExShowCastleInfo()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_CASTLE_INFO.writeId(this, buffer);
		final Collection<Castle> castles = CastleManager.getInstance().getCastles();
		buffer.writeInt(castles.size());
		for (Castle castle : castles)
		{
			buffer.writeInt(castle.getResidenceId());
			if (castle.getOwnerId() > 0)
			{
				if (ClanTable.getInstance().getClan(castle.getOwnerId()) != null)
				{
					buffer.writeString(ClanTable.getInstance().getClan(castle.getOwnerId()).getName());
				}
				else
				{
					PacketLogger.warning("Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
					buffer.writeString("");
				}
			}
			else
			{
				buffer.writeString("");
			}
			buffer.writeInt(castle.getTaxPercent());
			buffer.writeInt((int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
			buffer.writeByte(castle.getSiege().isInProgress()); // Grand Crusade
			// buffer.writeByte(castle.getSide().ordinal()); // Grand Crusade
		}
	}
}
