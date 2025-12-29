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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Atronic
 */
public class ExRequestPartyMatchingHistory extends ServerPacket
{
	private static final String GET_HISTORY = "SELECT title, leader FROM party_matching_history ORDER BY id DESC LIMIT 100";
	
	public ExRequestPartyMatchingHistory()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_MATCHING_ROOM_HISTORY.writeId(this, buffer);
		buffer.writeInt(100); // Maximum size according to retail.
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(GET_HISTORY);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				buffer.writeString(rset.getString("title"));
				buffer.writeString(rset.getString("leader"));
			}
		}
		catch (Exception e)
		{
			PacketLogger.warning("ExRequestPartyMatchingHistory: Could not load data: " + e.getMessage());
		}
	}
}
